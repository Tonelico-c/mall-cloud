<script setup>
  import orderApi from '@/api/order/order.js'
  import {ref} from 'vue'
  import {ElMessage, ElMessageBox} from 'element-plus'
  import {Refresh, Search, View, Van} from '@element-plus/icons-vue'

  //订单状态：0-已取消，1-未支付，2-已支付，3-已发货，4-交易成功，5-已关闭
  const statusList = [
    {value: 0, label: '已取消', type: 'info'},
    {value: 1, label: '未支付', type: 'warning'},
    {value: 2, label: '已支付', type: 'primary'},
    {value: 3, label: '已发货', type: 'primary'},
    {value: 4, label: '交易成功', type: 'success'},
    {value: 5, label: '已关闭', type: 'danger'}
  ]

  const statusOf = (status) => statusList.find(item => item.value === status) || {label: '未知状态', type: 'info'}

  //金额显示，保留两位小数
  const formatPrice = (price) => '¥' + Number(price || 0).toFixed(2)

  //表格数据
  const list = ref([])
  const total = ref(0)
  //分页信息和搜索条件
  const orderQuery = ref({
    orderNo: '',
    status: null,
    page: 1,
    limit: 10
  })

  // 时间范围
  const createTimeRange = ref([])
  const loadData = () => {
    orderQuery.value.beginCreateTime = createTimeRange.value?.[0];
    orderQuery.value.endCreateTime = createTimeRange.value?.[1];
    orderApi.list(orderQuery.value).then(result => {
      if (result.code === 1) {
        list.value = result.data.records
        total.value = result.data.total
      }
    })
  }

  loadData()

  const onSearch = () => {
    orderQuery.value.page = 1
    loadData()
  }
  const resetSearch = () => {
    createTimeRange.value = []
    orderQuery.value = {
      orderNo: '',
      status: null,
      page: 1,
      limit: 10
    }
    loadData()
  }

  //发货：只有"已支付"的订单才显示该操作，点击后订单状态变为"已发货"
  const deliver = (row) => {
    ElMessageBox.confirm(
        '您确认要对该订单发货么?',
        '提示',
        {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning',
          lockScroll: false //防止抖动
        }
    ).then(() => {
      orderApi.deliver(row.orderNo).then(result => {
        if (result.code === 1) {
          ElMessage.success(result.msg)
          loadData()
        } else {
          ElMessage.error(result.msg)
        }
      })
    })
  }

  //详情：查看当前订单的商品信息
  const dialogDetailVisible = ref(false)
  const orderDetail = ref({})
  const showDetailDialog = (orderNo) => {
    dialogDetailVisible.value = true
    orderDetail.value = {}
    orderApi.detail(orderNo).then(result => {
      if (result.code === 1) {
        orderDetail.value = result.data
      } else {
        ElMessage.error(result.msg)
      }
    })
  }
</script>

<template>
  <el-card class="">
    <template #header>
      <el-form :inline="true" class="search-form" @submit.prevent>
        <el-form-item label="订单号">
          <el-input
              v-model="orderQuery.orderNo"
              placeholder="请输入订单号"
              clearable
              style="width: 220px"
              @keyup.enter="onSearch"
          />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="orderQuery.status" placeholder="全部状态" clearable style="width: 160px">
            <el-option v-for="item in statusList" :key="item.value" :label="item.label" :value="item.value"/>
          </el-select>
        </el-form-item>
        <el-form-item label="下单时间">
          <el-date-picker
              v-model="createTimeRange"
              type="datetimerange"
              value-format="YYYY-MM-DD HH:mm:ss"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="onSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </template>
    <el-table :data="list" border style="width: 100%" show-overflow-tooltip>
      <el-table-column fixed prop="orderNo" label="订单号" width="180"/>
      <el-table-column prop="userId" label="用户ID" width="100"/>
      <el-table-column prop="payment" label="支付金额" width="120">
        <template #default="{row}">
          <span class="order-price">{{ formatPrice(row.payment) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="订单状态" width="120">
        <template #default="{row}">
          <el-tag :type="statusOf(row.status).type">{{ statusOf(row.status).label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="paymentTime" label="支付时间" width="200"/>
      <el-table-column prop="sendTime" label="发货时间" width="200"/>
      <el-table-column prop="createTime" label="下单时间" width="200"/>
      <el-table-column align="center" width="220" fixed="right" label="操作">
        <template #default="{ row }">
          <el-button size="small" type="primary" :icon="View" @click="showDetailDialog(row.orderNo)">详情</el-button>
          <!-- 只有已支付的订单才多一个发货操作 -->
          <el-button
              v-if="row.status === 2"
              size="small"
              type="success"
              :icon="Van"
              @click="deliver(row)">发货
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination-wrapper">
      <el-pagination
          v-model:current-page="orderQuery.page"
          v-model:page-size="orderQuery.limit"
          :page-sizes="[10, 20, 30, 40]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @change="loadData"
      />
    </div>
  </el-card>

  <!--订单详情弹出框：订单基本信息 + 商品信息-->
  <el-dialog v-model="dialogDetailVisible" title="订单详情" width="760" :lock-scroll="false">
    <el-descriptions :column="2" border class="order-descriptions">
      <el-descriptions-item label="订单号">{{ orderDetail.orderNo }}</el-descriptions-item>
      <el-descriptions-item label="用户ID">{{ orderDetail.userId }}</el-descriptions-item>
      <el-descriptions-item label="订单状态">
        <el-tag :type="statusOf(orderDetail.status).type">{{ statusOf(orderDetail.status).label }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="支付金额">{{ formatPrice(orderDetail.payment) }}</el-descriptions-item>
      <el-descriptions-item label="支付时间">{{ orderDetail.paymentTime }}</el-descriptions-item>
      <el-descriptions-item label="下单时间">{{ orderDetail.createTime }}</el-descriptions-item>
    </el-descriptions>
    <div class="detail-title">商品信息</div>
    <el-table :data="orderDetail.orderItemList" border style="width: 100%">
      <el-table-column prop="productImage" label="商品图片" width="110">
        <template #default="{row}">
          <img :src="row.productImage" alt="商品图片" class="table-image" v-if="row.productImage"/>
        </template>
      </el-table-column>
      <el-table-column prop="productName" label="商品名称" min-width="200"/>
      <el-table-column prop="currentUnitPrice" label="单价" width="120">
        <template #default="{row}">
          {{ formatPrice(row.currentUnitPrice) }}
        </template>
      </el-table-column>
      <el-table-column prop="quantity" label="数量" width="80"/>
      <el-table-column prop="totalPrice" label="小计" width="130">
        <template #default="{row}">
          {{ formatPrice(row.totalPrice) }}
        </template>
      </el-table-column>
    </el-table>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogDetailVisible = false">关闭</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.search-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 14px 18px;
}

.search-form :deep(.el-form-item) {
  margin: 0;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.order-price {
  color: #f56c6c;
  font-weight: 600;
}

.order-descriptions {
  margin-bottom: 16px;
}

.detail-title {
  margin-bottom: 12px;
  font-size: 14px;
  font-weight: 600;
  color: #1f2d3d;
}

.table-image {
  width: 60px;
  height: 60px;
  display: block;
  border-radius: 6px;
  object-fit: cover;
  margin: 0 auto;
  border: 1px solid #eef2f5;
}
</style>
