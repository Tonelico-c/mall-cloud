<script setup>
  import {ref, onMounted} from 'vue'
  import {useRouter} from 'vue-router'
  import {Loading} from '@element-plus/icons-vue'
  import {ElMessage, ElMessageBox} from 'element-plus'
  import orderApi from '@/api/order/order.js'
  import PageHeader from '@/components/PageHeader.vue'
  import {formatImage, formatPrice} from '@/utils/format.js'

  const router = useRouter()

  //订单状态：0-已取消，1-未付款，2-已付款，3-已发货，4-交易成功，5-交易关闭
  const statusMap = {
    0: {text: '已取消', type: 'info'},
    1: {text: '待付款', type: 'warning'},
    2: {text: '已付款', type: 'primary'},
    3: {text: '已发货', type: 'primary'},
    4: {text: '交易成功', type: 'success'},
    5: {text: '交易关闭', type: 'danger'}
  }

  //订单列表
  const list = ref([])
  const loading = ref(true)

  onMounted(() => {
    loadList()
  })

  const loadList = () => {
    loading.value = true
    orderApi.list().then(result => {
      if (result.code === 1) {
        list.value = result.data || []
      }
      loading.value = false
    }).catch(() => {
      loading.value = false
    })
  }

  //去支付：弹出确认弹窗，确认后订单状态由"未支付"变为"已支付"
  const pay = (order) => {
    ElMessageBox.confirm(
        `您确认支付 ${formatPrice(order.payment)} 么?`,
        '支付确认',
        {
          confirmButtonText: '确认支付',
          cancelButtonText: '取消',
          type: 'warning'
        }
    ).then(() => {
      orderApi.pay(order.orderNo).then(result => {
        if (result.code === 1) {
          ElMessage.success(result.msg)
          loadList()
        } else {
          ElMessage.error(result.msg)
        }
      })
    }).catch(() => {
      //点了取消会走这里，兜住避免控制台 unhandled rejection
    })
  }

  //确认收货：订单状态由"已发货"变为"交易成功"
  const receive = (order) => {
    ElMessageBox.confirm(
        '您确认已经收到商品么?',
        '提示',
        {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning'
        }
    ).then(() => {
      orderApi.receive(order.orderNo).then(result => {
        if (result.code === 1) {
          ElMessage.success(result.msg)
          loadList()
        } else {
          ElMessage.error(result.msg)
        }
      })
    }).catch(() => {
      //点了取消会走这里，兜住避免控制台 unhandled rejection
    })
  }

  const toHome = () => router.push('/home')
</script>

<template>
  <div class="order-list">
    <PageHeader title="我的订单"/>

    <!-- 加载中 -->
    <div v-if="loading" class="list-loading">
      <el-icon class="is-loading"><Loading/></el-icon>
      <span>加载中...</span>
    </div>

    <!-- 空状态 -->
    <el-empty v-if="!loading && list.length === 0" description="暂无订单">
      <el-button type="primary" round @click="toHome">去逛逛</el-button>
    </el-empty>

    <!-- 订单列表 -->
    <div v-if="!loading" class="orders">
      <div v-for="order in list" :key="order.orderNo" class="order-card card">
        <!-- 订单头部：订单号 + 状态 -->
        <div class="order-header">
          <span class="order-no">订单号：{{ order.orderNo }}</span>
          <el-tag size="small" :type="statusMap[order.status]?.type || 'info'">
            {{ statusMap[order.status]?.text || '未知状态' }}
          </el-tag>
        </div>
        <!-- 订单商品 -->
        <div v-for="item in order.orderItemList" :key="item.id" class="order-item">
          <img class="order-img" :src="formatImage(item.productImage)">
          <div class="order-info">
            <div class="order-name ellipsis-2">{{ item.productName }}</div>
          </div>
          <div class="order-right">
            <span class="order-price">{{ formatPrice(item.currentUnitPrice) }}</span>
            <span class="order-count">×{{ item.quantity }}</span>
          </div>
        </div>
        <!-- 订单底部：下单时间 + 实付金额 -->
        <div class="order-footer">
          <span class="order-time">{{ order.createTime }}</span>
          <div class="order-actions">
            <span class="order-pay">实付 <span class="price">{{ formatPrice(order.payment) }}</span></span>
            <!-- 未支付的订单可以去支付 -->
            <el-button
                v-if="order.status === 1"
                type="danger"
                size="small"
                round
                @click="pay(order)">去支付
            </el-button>
            <!-- 已发货的订单才可以确认收货 -->
            <el-button
                v-if="order.status === 3"
                type="primary"
                size="small"
                round
                @click="receive(order)">确认收货
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
  .orders {
    padding: 8px;
  }

  .order-card {
    padding: 12px;
    margin-bottom: 8px;
  }

  .order-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .order-no {
    font-size: 12px;
    color: #999;
  }

  .order-item {
    display: flex;
    gap: 10px;
    padding: 12px 0 0;
  }

  .order-img {
    width: 64px;
    height: 64px;
    flex-shrink: 0;
    border-radius: 6px;
    object-fit: contain;
    background-color: #fafafa;
  }

  .order-info {
    flex: 1;
    min-width: 0;
  }

  .order-name {
    font-size: 14px;
    color: #303133;
    line-height: 1.4;
  }

  .order-right {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    flex-shrink: 0;
  }

  .order-price {
    font-size: 14px;
    color: #303133;
  }

  .order-count {
    margin-top: 4px;
    font-size: 12px;
    color: #999;
  }

  .order-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 12px;
    padding-top: 10px;
    border-top: 1px solid #f5f5f5;
  }

  .order-time {
    font-size: 12px;
    color: #999;
  }

  .order-pay {
    display: flex;
    align-items: baseline;
    font-size: 13px;
    color: #666;
  }

  /* 实付金额 + 确认收货按钮 */
  .order-actions {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .order-pay .price {
    font-size: 17px;
  }
</style>
