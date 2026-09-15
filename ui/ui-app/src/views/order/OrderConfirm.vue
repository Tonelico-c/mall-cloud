<script setup>
  import {ref, computed, onMounted} from 'vue'
  import {useRouter} from 'vue-router'
  import {ArrowRight, Check, Loading} from '@element-plus/icons-vue'
  import {ElMessage} from 'element-plus'
  import cartApi from '@/api/cart/cart.js'
  import orderApi from '@/api/order/order.js'
  import shippingApi from '@/api/shipping/shipping.js'
  import PageHeader from '@/components/PageHeader.vue'
  import {useCartCountStore} from '@/store/cartCount.js'
  import {formatImage, formatPrice} from '@/utils/format.js'

  const router = useRouter()
  const cartCountStore = useCartCountStore()

  //购物车中已勾选的商品
  const items = ref([])
  const loading = ref(true)

  const loadItems = () => {
    loading.value = true
    cartApi.list().then(result => {
      if (result.code === 1) {
        items.value = (result.data || []).filter(item => item.product && item.selected === 1)
      }
      loading.value = false
    }).catch(() => {
      loading.value = false
    })
  }

  //收货地址
  const shippingList = ref([])
  const addressIndex = ref(0)

  const loadShipping = () => {
    shippingApi.list().then(result => {
      if (result.code === 1) {
        shippingList.value = result.data || []
        //默认选中默认地址，没有默认地址时选中第一个
        const defaultIndex = shippingList.value.findIndex(shipping => shipping.isDefault === 1)
        addressIndex.value = defaultIndex >= 0 ? defaultIndex : 0
      }
    })
  }

  onMounted(() => {
    loadItems()
    loadShipping()
  })

  //当前选中的收货地址
  const currentAddress = computed(() => shippingList.value[addressIndex.value])

  //完整地址：省市区 + 详细地址（部分字段可能为空）
  const fullAddress = (shipping) =>
      [shipping.receiverProvince, shipping.receiverCity, shipping.receiverDistrict, shipping.receiverAddress]
          .filter(Boolean).join(' ')

  //选择地址的弹窗
  const dialogVisible = ref(false)
  const chooseAddress = (index) => {
    addressIndex.value = index
    dialogVisible.value = false
  }

  //合计
  const totalCount = computed(() => items.value.reduce((sum, item) => sum + item.count, 0))
  const totalPrice = computed(() => items.value.reduce((sum, item) => sum + item.product.price * item.count, 0))

  //提交订单
  const submitting = ref(false)
  const submitOrder = () => {
    if (!currentAddress.value) {
      ElMessage.warning('请先选择收货地址')
      return
    }
    submitting.value = true
    orderApi.create({shippingId: currentAddress.value.id}).then(result => {
      submitting.value = false
      if (result.code === 1) {
        ElMessage.success('下单成功')
        //下单后购物车已勾选的商品会被清除，刷新角标
        cartCountStore.refreshCount()
        router.replace('/order')
      } else {
        ElMessage.error(result.msg || '下单失败')
      }
    }).catch(() => {
      submitting.value = false
    })
  }

  const toShipping = () => router.push('/shipping')
  const toCart = () => router.push('/cart')
</script>

<template>
  <div class="confirm pb-action">
    <PageHeader title="确认订单"/>

    <!-- 加载中 -->
    <div v-if="loading" class="list-loading">
      <el-icon class="is-loading"><Loading/></el-icon>
      <span>加载中...</span>
    </div>

    <template v-if="!loading && items.length > 0">
      <!-- 收货地址 -->
      <div class="addr-card card" @click="dialogVisible = true">
        <template v-if="currentAddress">
          <div class="addr-row">
            <span class="addr-name">{{ currentAddress.receiverName }}</span>
            <span class="addr-mobile">{{ currentAddress.receiverMobile }}</span>
            <el-icon class="addr-arrow"><ArrowRight/></el-icon>
          </div>
          <div class="addr-detail">{{ fullAddress(currentAddress) }}</div>
        </template>
        <div v-else class="addr-empty">
          <span>请添加收货地址</span>
          <el-icon class="addr-arrow"><ArrowRight/></el-icon>
        </div>
      </div>

      <!-- 商品清单 -->
      <div class="goods-card card">
        <div v-for="item in items" :key="item.id" class="goods-item">
          <img class="goods-img" :src="formatImage(item.product.mainImage)">
          <div class="goods-info">
            <div class="goods-name ellipsis-2">{{ item.product.name }}</div>
            <div class="goods-sub ellipsis-1" v-if="item.product.subtitle">{{ item.product.subtitle }}</div>
          </div>
          <div class="goods-right">
            <span class="goods-price">{{ formatPrice(item.product.price) }}</span>
            <span class="goods-count">×{{ item.count }}</span>
          </div>
        </div>
        <div class="goods-total">
          <span>共 {{ totalCount }} 件商品，合计：</span>
          <span class="price">{{ formatPrice(totalPrice) }}</span>
        </div>
      </div>
    </template>

    <!-- 没有已勾选的商品 -->
    <el-empty v-if="!loading && items.length === 0" description="没有已勾选的商品">
      <el-button type="primary" round @click="toCart">去购物车</el-button>
    </el-empty>

    <!-- 底部提交栏 -->
    <div v-if="!loading && items.length > 0" class="confirm-bar app-fixed">
      <div class="confirm-total">
        <span class="confirm-label">合计</span>
        <span class="price">{{ formatPrice(totalPrice) }}</span>
      </div>
      <el-button class="confirm-button" type="primary" round :loading="submitting" @click="submitOrder">
        提交订单
      </el-button>
    </div>

    <!-- 选择收货地址弹窗 -->
    <el-dialog v-model="dialogVisible" title="选择收货地址" width="92%">
      <div v-if="shippingList.length > 0" class="addr-list">
        <div v-for="(shipping, index) in shippingList" :key="shipping.id" class="addr-option"
             :class="{active: index === addressIndex}" @click="chooseAddress(index)">
          <div class="addr-row">
            <span class="addr-name">{{ shipping.receiverName }}</span>
            <span class="addr-mobile">{{ shipping.receiverMobile }}</span>
          </div>
          <div class="addr-detail">{{ fullAddress(shipping) }}</div>
          <el-icon v-if="index === addressIndex" class="addr-option-check" :size="18"><Check/></el-icon>
        </div>
      </div>
      <el-empty v-else description="暂无收货地址"/>
      <el-button class="addr-manage" text type="primary" @click="toShipping">管理收货地址</el-button>
    </el-dialog>
  </div>
</template>

<style scoped>
  .addr-card {
    margin: 8px;
    padding: 14px 12px;
  }

  .addr-row {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .addr-name {
    font-size: 15px;
    font-weight: 600;
    color: #303133;
  }

  .addr-mobile {
    font-size: 13px;
    color: #666;
  }

  .addr-arrow {
    margin-left: auto;
    color: #999;
  }

  .addr-detail {
    margin-top: 6px;
    font-size: 13px;
    color: #606266;
    line-height: 1.5;
  }

  .addr-empty {
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    color: #999;
    padding: 8px 0;
  }

  /* 商品清单 */
  .goods-card {
    margin: 8px;
    padding: 4px 12px;
  }

  .goods-item {
    display: flex;
    gap: 10px;
    padding: 12px 0;
    border-bottom: 1px solid #f5f5f5;
  }

  .goods-img {
    width: 64px;
    height: 64px;
    flex-shrink: 0;
    border-radius: 6px;
    object-fit: contain;
    background-color: #fafafa;
  }

  .goods-info {
    flex: 1;
    min-width: 0;
  }

  .goods-name {
    font-size: 14px;
    color: #303133;
    line-height: 1.4;
  }

  .goods-sub {
    margin-top: 2px;
    font-size: 12px;
    color: #999;
  }

  .goods-right {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    flex-shrink: 0;
  }

  .goods-price {
    font-size: 14px;
    font-weight: 600;
    color: var(--el-color-primary);
  }

  .goods-count {
    margin-top: 4px;
    font-size: 12px;
    color: #999;
  }

  .goods-total {
    display: flex;
    align-items: baseline;
    justify-content: flex-end;
    gap: 4px;
    padding: 12px 0;
    font-size: 13px;
    color: #666;
  }

  .goods-total .price {
    font-size: 17px;
  }

  /* 底部提交栏 */
  .confirm-bar {
    bottom: 0;
    display: flex;
    align-items: center;
    height: calc(50px + env(safe-area-inset-bottom));
    padding: 0 12px env(safe-area-inset-bottom);
    box-sizing: border-box;
    background-color: #fff;
    border-top: 1px solid #f0f0f0;
  }

  .confirm-total {
    display: flex;
    align-items: baseline;
    gap: 4px;
    margin-right: auto;
  }

  .confirm-label {
    font-size: 12px;
    color: #666;
  }

  .confirm-total .price {
    font-size: 20px;
  }

  .confirm-button {
    height: 38px;
    font-size: 15px;
    font-weight: 600;
    padding: 0 28px;
  }

  /* 地址选择弹窗 */
  .addr-list {
    max-height: 50vh;
    overflow-y: auto;
  }

  .addr-option {
    position: relative;
    padding: 12px;
    margin-bottom: 8px;
    border: 1px solid #eee;
    border-radius: var(--app-card-radius);
  }

  .addr-option.active {
    border-color: var(--el-color-primary);
    background-color: var(--el-color-primary-light-9);
  }

  .addr-option-check {
    position: absolute;
    right: 12px;
    top: 50%;
    transform: translateY(-50%);
    color: var(--el-color-primary);
  }

  .addr-manage {
    width: 100%;
  }
</style>
