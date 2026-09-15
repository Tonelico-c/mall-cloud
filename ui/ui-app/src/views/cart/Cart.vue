<script setup>
  import {ref, computed, onMounted} from 'vue'
  import {useRouter} from 'vue-router'
  import {Delete, Loading} from '@element-plus/icons-vue'
  import {ElMessage, ElMessageBox} from 'element-plus'
  import cartApi from '@/api/cart/cart.js'
  import PageHeader from '@/components/PageHeader.vue'
  import {useCartCountStore} from '@/store/cartCount.js'
  import {formatImage, formatPrice} from '@/utils/format.js'

  const router = useRouter()
  const cartCountStore = useCartCountStore()

  //购物车列表
  const list = ref([])
  const loading = ref(true)

  //购物车项对应的商品是否有效（商品可能已下架或删除）
  const isValid = (item) => !!item.product

  const loadData = () => {
    loading.value = true
    cartApi.list().then(result => {
      if (result.code === 1) {
        list.value = result.data || []
        //同步底部TabBar的购物车角标
        cartCountStore.setCount(list.value.reduce((sum, item) => sum + (item.count || 0), 0))
      }
      loading.value = false
    }).catch(() => {
      loading.value = false
    })
  }
  onMounted(loadData)

  //修改购买数量
  const updateCount = (item, value) => {
    const oldCount = item.count
    item.count = value
    cartCountStore.setCount(list.value.reduce((sum, item) => sum + (item.count || 0), 0))
    cartApi.update({id: item.id, count: value, selected: item.selected}).then(result => {
      if (result.code !== 1) {
        //失败回滚
        item.count = oldCount
        ElMessage.error(result.msg || '修改数量失败')
      }
    }).catch(() => {
      item.count = oldCount
    })
  }

  //修改选中状态
  const updateSelected = (item, value) => {
    const oldSelected = item.selected
    item.selected = value ? 1 : 0
    cartApi.update({id: item.id, count: item.count, selected: item.selected}).then(result => {
      if (result.code !== 1) {
        item.selected = oldSelected
        ElMessage.error(result.msg || '操作失败')
      }
    }).catch(() => {
      item.selected = oldSelected
    })
  }

  //全选/取消全选
  const isAllSelected = computed({
    get: () => {
      const validItems = list.value.filter(isValid)
      return validItems.length > 0 && validItems.every(item => item.selected === 1)
    },
    set: (value) => {
      list.value.forEach(item => {
        if (isValid(item)) {
          item.selected = value ? 1 : 0
        }
      })
      //逐项同步到后台
      list.value.filter(isValid).forEach(item => {
        cartApi.update({id: item.id, count: item.count, selected: item.selected})
      })
    }
  })

  //已勾选的商品、总数量、总金额
  const selectedItems = computed(() => list.value.filter(item => isValid(item) && item.selected === 1))
  const selectedCount = computed(() => selectedItems.value.reduce((sum, item) => sum + item.count, 0))
  const totalPrice = computed(() => selectedItems.value.reduce((sum, item) => sum + item.product.price * item.count, 0))

  //删除购物车项
  const deleteById = (item) => {
    ElMessageBox.confirm('确认将该商品移出购物车吗？', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning',
      lockScroll: false //防止抖动
    }).then(() => {
      cartApi.deleteById(item.id).then(result => {
        if (result.code === 1) {
          ElMessage.success('删除成功')
          loadData()
        } else {
          ElMessage.error(result.msg || '删除失败')
        }
      })
    }).catch(() => {
    })
  }

  //去结算
  const toConfirm = () => {
    if (selectedItems.value.length === 0) {
      ElMessage.warning('请勾选要结算的商品')
      return
    }
    router.push('/order/confirm')
  }

  //去逛逛
  const toHome = () => router.push('/home')
</script>

<template>
  <div class="cart pb-action-double">
    <PageHeader title="购物车" :back="false"/>

    <!-- 加载中 -->
    <div v-if="loading" class="list-loading">
      <el-icon class="is-loading"><Loading/></el-icon>
      <span>加载中...</span>
    </div>

    <!-- 空购物车 -->
    <el-empty v-if="!loading && list.length === 0" description="购物车还是空的">
      <el-button type="primary" round @click="toHome">去逛逛</el-button>
    </el-empty>

    <!-- 购物车列表 -->
    <div v-if="!loading" class="cart-list">
      <div v-for="item in list" :key="item.id" class="cart-item card" :class="{invalid: !isValid(item)}">
        <!-- 勾选框 -->
        <el-checkbox class="cart-check" :model-value="item.selected === 1" :disabled="!isValid(item)"
                     @change="(value) => updateSelected(item, value)"/>
        <!-- 商品图片 -->
        <img class="cart-img" :src="formatImage(item.product?.mainImage)" @click="router.push('/product/' + item.productId)">
        <!-- 商品信息 -->
        <div class="cart-info">
          <div class="cart-name ellipsis-2" @click="router.push('/product/' + item.productId)">
            {{ isValid(item) ? item.product.name : '商品已失效' }}
          </div>
          <div v-if="isValid(item) && item.product.subtitle" class="cart-sub ellipsis-1">{{ item.product.subtitle }}</div>
          <div class="cart-bottom">
            <span class="cart-price">{{ isValid(item) ? formatPrice(item.product.price) : '' }}</span>
            <div class="cart-ops">
              <el-input-number v-if="isValid(item)" size="small" :model-value="item.count"
                               :min="1" :max="Math.max(1, item.product.stock || 1)"
                               @change="(value) => updateCount(item, value)"/>
              <el-icon class="cart-delete" :size="16" @click="deleteById(item)"><Delete/></el-icon>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部结算栏，位于TabBar上方 -->
    <div v-if="list.length > 0" class="cart-bar app-fixed">
      <el-checkbox class="cart-bar-check" v-model="isAllSelected">全选</el-checkbox>
      <div class="cart-bar-total">
        <span class="cart-bar-label">合计</span>
        <span class="price">{{ formatPrice(totalPrice) }}</span>
      </div>
      <el-button class="cart-bar-button" type="primary" round @click="toConfirm">
        去结算({{ selectedCount }})
      </el-button>
    </div>
  </div>
</template>

<style scoped>
  .cart-item {
    display: flex;
    align-items: center;
    gap: 10px;
    margin: 8px;
    padding: 12px;
  }

  .cart-item.invalid {
    opacity: 0.6;
  }

  .cart-check {
    flex-shrink: 0;
    height: auto;
  }

  .cart-img {
    width: 90px;
    height: 90px;
    flex-shrink: 0;
    border-radius: 6px;
    object-fit: contain;
    background-color: #fafafa;
  }

  .cart-info {
    flex: 1;
    min-width: 0;
  }

  .cart-name {
    font-size: 14px;
    color: #303133;
    line-height: 1.4;
  }

  .cart-sub {
    margin-top: 2px;
    font-size: 12px;
    color: #999;
  }

  .cart-bottom {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 8px;
  }

  .cart-price {
    font-size: 15px;
    font-weight: 600;
    color: var(--el-color-primary);
  }

  .cart-ops {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .cart-delete {
    color: #999;
    padding: 4px;
  }

  /* 底部结算栏，叠在TabBar正上方 */
  .cart-bar {
    bottom: calc(50px + env(safe-area-inset-bottom));
    display: flex;
    align-items: center;
    height: 50px;
    padding: 0 12px;
    box-sizing: border-box;
    background-color: #fff;
    border-top: 1px solid #f0f0f0;
    z-index: 99;
  }

  .cart-bar-check {
    margin-right: auto;
  }

  .cart-bar-total {
    display: flex;
    align-items: baseline;
    gap: 4px;
    margin-right: 10px;
  }

  .cart-bar-label {
    font-size: 12px;
    color: #666;
  }

  .cart-bar-total .price {
    font-size: 18px;
  }

  .cart-bar-button {
    height: 36px;
    font-size: 14px;
    font-weight: 600;
    padding: 0 20px;
  }
</style>
