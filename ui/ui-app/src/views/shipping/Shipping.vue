<script setup>
  import {ref, onMounted} from 'vue'
  import {Loading} from '@element-plus/icons-vue'
  import {ElMessage, ElMessageBox} from 'element-plus'
  import shippingApi from '@/api/shipping/shipping.js'
  import PageHeader from '@/components/PageHeader.vue'

  //收货地址列表
  const list = ref([])
  const loading = ref(true)

  const loadData = () => {
    loading.value = true
    shippingApi.list().then(result => {
      if (result.code === 1) {
        list.value = result.data || []
      }
      loading.value = false
    }).catch(() => {
      loading.value = false
    })
  }
  onMounted(loadData)

  //新增/编辑弹窗
  const dialogVisible = ref(false)
  const isEdit = ref(false)
  const title = ref('新增收货地址')
  const shippingForm = ref({})

  const showAdd = () => {
    isEdit.value = false
    title.value = '新增收货地址'
    shippingForm.value = {isDefault: 0}
    dialogVisible.value = true
  }

  const showEdit = (shipping) => {
    isEdit.value = true
    title.value = '编辑收货地址'
    //拷贝一份，避免修改弹窗时直接改动列表数据
    shippingForm.value = {...shipping}
    dialogVisible.value = true
  }

  //表单校验模型
  const rules = ref({
    receiverName: [
      {required: true, message: '请输入收货人姓名', trigger: 'blur'}
    ],
    receiverMobile: [
      {required: true, message: '请输入手机号', trigger: 'blur'},
      {pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur'}
    ],
    receiverProvince: [
      {required: true, message: '请输入省份', trigger: 'blur'}
    ],
    receiverCity: [
      {required: true, message: '请输入城市', trigger: 'blur'}
    ],
    receiverDistrict: [
      {required: true, message: '请输入区/县', trigger: 'blur'}
    ],
    receiverAddress: [
      {required: true, message: '请输入详细地址', trigger: 'blur'}
    ]
  })
  const form = ref()

  const submit = (formEl) => {
    formEl.validate((valid) => {
      if (!valid) {
        return
      }
      const request = isEdit.value
          ? shippingApi.update(shippingForm.value.id, shippingForm.value)
          : shippingApi.add(shippingForm.value)
      request.then(result => {
        if (result.code === 1) {
          ElMessage.success(result.msg || '保存成功')
          dialogVisible.value = false
          loadData()
        } else {
          ElMessage.error(result.msg || '保存失败')
        }
      })
    })
  }

  //设为默认地址
  const setDefault = (shipping) => {
    shippingApi.setDefault(shipping.id).then(result => {
      if (result.code === 1) {
        ElMessage.success('已设为默认')
        loadData()
      } else {
        ElMessage.error(result.msg || '设置失败')
      }
    })
  }

  //删除收货地址
  const deleteById = (shipping) => {
    ElMessageBox.confirm('确认删除该收货地址吗？', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning',
      lockScroll: false //防止抖动
    }).then(() => {
      shippingApi.deleteById(shipping.id).then(result => {
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
</script>

<template>
  <div class="shipping">
    <PageHeader title="收货地址">
      <template #right>
        <span class="add-link" @click="showAdd">新增</span>
      </template>
    </PageHeader>

    <!-- 加载中 -->
    <div v-if="loading" class="list-loading">
      <el-icon class="is-loading"><Loading/></el-icon>
      <span>加载中...</span>
    </div>

    <!-- 空状态 -->
    <el-empty v-if="!loading && list.length === 0" description="暂无收货地址">
      <el-button type="primary" round @click="showAdd">新增地址</el-button>
    </el-empty>

    <!-- 地址列表 -->
    <div v-if="!loading" class="shipping-list">
      <div v-for="shipping in list" :key="shipping.id" class="shipping-card card">
        <div class="shipping-row">
          <span class="shipping-name">{{ shipping.receiverName }}</span>
          <el-tag v-if="shipping.isDefault === 1" size="small" round>默认</el-tag>
          <span class="shipping-mobile">{{ shipping.receiverMobile }}</span>
        </div>
        <div class="shipping-detail">
          {{ [shipping.receiverProvince, shipping.receiverCity, shipping.receiverDistrict,
              shipping.receiverAddress].filter(Boolean).join(' ') }}
        </div>
        <div v-if="shipping.receiverZip" class="shipping-zip">邮编：{{ shipping.receiverZip }}</div>
        <div class="shipping-ops">
          <el-button v-if="shipping.isDefault !== 1" text size="small" @click="setDefault(shipping)">设为默认</el-button>
          <el-button text size="small" @click="showEdit(shipping)">编辑</el-button>
          <el-button text size="small" type="danger" @click="deleteById(shipping)">删除</el-button>
        </div>
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="title" width="92%">
      <el-form ref="form" label-position="top" :model="shippingForm" :rules="rules" @submit.prevent>
        <el-form-item label="收货人" prop="receiverName">
          <el-input v-model="shippingForm.receiverName" placeholder="请输入收货人姓名"/>
        </el-form-item>
        <el-form-item label="手机号" prop="receiverMobile">
          <el-input v-model="shippingForm.receiverMobile" placeholder="请输入手机号" maxlength="11"/>
        </el-form-item>
        <div class="region-row">
          <el-form-item label="省份" prop="receiverProvince">
            <el-input v-model="shippingForm.receiverProvince" placeholder="省份"/>
          </el-form-item>
          <el-form-item label="城市" prop="receiverCity">
            <el-input v-model="shippingForm.receiverCity" placeholder="城市"/>
          </el-form-item>
          <el-form-item label="区/县" prop="receiverDistrict">
            <el-input v-model="shippingForm.receiverDistrict" placeholder="区/县"/>
          </el-form-item>
        </div>
        <el-form-item label="详细地址" prop="receiverAddress">
          <el-input v-model="shippingForm.receiverAddress" placeholder="街道、门牌号等" type="textarea" :rows="2"/>
        </el-form-item>
        <el-form-item label="邮编">
          <el-input v-model="shippingForm.receiverZip" placeholder="选填" maxlength="6"/>
        </el-form-item>
        <el-form-item label="设为默认地址">
          <el-switch v-model="shippingForm.isDefault" :active-value="1" :inactive-value="0"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit(form)">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
  .add-link {
    color: var(--el-color-primary);
    font-weight: 500;
  }

  .shipping-list {
    padding: 8px;
  }

  .shipping-card {
    padding: 14px 12px;
    margin-bottom: 8px;
  }

  .shipping-row {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .shipping-name {
    font-size: 15px;
    font-weight: 600;
    color: #303133;
  }

  .shipping-mobile {
    font-size: 13px;
    color: #666;
  }

  .shipping-detail {
    margin-top: 6px;
    font-size: 13px;
    color: #606266;
    line-height: 1.5;
  }

  .shipping-zip {
    margin-top: 4px;
    font-size: 12px;
    color: #999;
  }

  .shipping-ops {
    display: flex;
    justify-content: flex-end;
    margin-top: 6px;
  }

  /* 省市区一行三列 */
  .region-row {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 8px;
  }
</style>
