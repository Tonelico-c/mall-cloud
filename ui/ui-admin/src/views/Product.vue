<script setup>
  import productApi from '@/api/product.js'
  import {ref} from 'vue'
  import {ElMessage, ElMessageBox} from 'element-plus'
  import {Delete, Edit, Plus, Search, Refresh} from '@element-plus/icons-vue'
  import {useTokenStore} from '@/store/token.js'
  const tokenStore = useTokenStore();

  //表格数据
  const list = ref([])
  const total = ref(0)
  //分页信息和搜索条件
  const productQuery = ref({
    name: '',
    subtitle: '',
    page: 1,
    limit: 10
  })

  const loadData = () => {
    productApi.list(productQuery.value).then(result => {
      list.value = result.data.records
      total.value = result.data.total
    })
  }

  loadData()

  const onSearch = () => {
    productQuery.value.page = 1
    loadData()
  }
  const resetSearch = () => {
    productQuery.value = {
      name: '',
      subtitle: '',
      page: 1,
      limit: 10
    }
    loadData()
  }

  //根据id删除
  const deleteById = (id) => {
    ElMessageBox.confirm(
        '您确认要删除么?',
        '警告',
        {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning',
          lockScroll: false //防止抖动
        }
    ).then(() => {
      productApi.deleteById(id).then(result => {
        if (result.code === 1) {
          ElMessage.success(result.msg)
          loadData()
        } else {
          ElMessage.error(result.msg)
        }
      })
    })
  }

  let ids = []
  const handleSelectionChange = (rows) => {
    ids = rows.map(row => row.id)
  }

  const deleteAll = () => {
    if (ids.length === 0) {
      ElMessage.warning('请先选择要删除的数据')
      return
    }
    ElMessageBox.confirm(
        '您确认要删除选中的数据么?',
        '警告',
        {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning',
          lockScroll: false //防止抖动
        }
    ).then(() => {
      productApi.deleteAll(ids).then(result => {
        if (result.code === 1) {
          ElMessage.success(result.msg)
          loadData()
        } else {
          ElMessage.error(result.msg)
        }
      })
    })
  }

  //添加、编辑
  const dialogFormVisible = ref(false)
  const product = ref({})
  const title = ref()

  const showAddDialog = () => {
    dialogFormVisible.value = true
    title.value = '添加'
    product.value = {status: 1}
  }

  const showUpdateDialog = (id) => {
    dialogFormVisible.value = true
    title.value = '编辑'
    product.value = {}
    productApi.selectById(id).then(result => {
      product.value = result.data
    })
  }

  const addOrUpdate = () => {
    if (product.value.id) {//编辑
      productApi.update(product.value.id, product.value).then(result => {
        if (result.code === 1) {
          ElMessage.success(result.msg)
          dialogFormVisible.value = false
          loadData()
        } else {
          ElMessage.error(result.msg)
        }
      })
    } else {//添加
      productApi.add(product.value).then(result => {
        if (result.code === 1) {
          ElMessage.success(result.msg)
          dialogFormVisible.value = false
          loadData()
        } else {
          ElMessage.error(result.msg)
        }
      })
    }
  }

  // 主图上传成功
  const handleMainImageSuccess = (result) => {
    if (result.code === 1) {
      product.value.mainImage = result.data
    } else {
      ElMessage.error(result.msg)
    }
  }
</script>

<template>
  <el-card class="">
    <template #header>
      <el-form :inline="true" class="search-form" @submit.prevent>
        <el-form-item label="商品名称">
          <el-input
              v-model="productQuery.name"
              placeholder="请输入商品名称"
              clearable
              style="width: 220px"
              @keyup.enter="onSearch"
          />
        </el-form-item>
        <el-form-item label="副标题">
          <el-input
              v-model="productQuery.subtitle"
              placeholder="请输入副标题"
              clearable
              style="width: 220px"
              @keyup.enter="onSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="onSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </template>
    <div class="toolbar">
      <el-button type="primary" :icon="Plus" @click="showAddDialog">添加</el-button>
      <el-button type="danger" :icon="Delete" @click="deleteAll">批量删除</el-button>
    </div>
    <el-table :data="list" border style="width: 100%" show-overflow-tooltip @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" />
      <el-table-column fixed prop="id" label="ID" width="100"/>
      <el-table-column prop="mainImage" label="主图" width="100">
        <template #default="{row}">
          <img :src="row.mainImage" alt="主图" class="table-image" v-if="row.mainImage"/>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="商品名称" width="180"/>
      <el-table-column prop="subtitle" label="副标题" width="200"/>
      <el-table-column prop="categoryId" label="分类ID" width="100"/>
      <el-table-column prop="price" label="价格(元)" width="120"/>
      <el-table-column prop="stock" label="库存" width="100"/>
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '在售' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="200"/>
      <el-table-column align="center" width="200" fixed="right" label="操作">
        <template #default="{ row }">
          <el-button size="small" type="primary" :icon="Edit" @click="showUpdateDialog(row.id)">编辑</el-button>
          <el-button size="small" type="danger" :icon="Delete" @click="deleteById(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination-wrapper">
      <el-pagination
          v-model:current-page="productQuery.page"
          v-model:page-size="productQuery.limit"
          :page-sizes="[10, 20, 30, 40]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @change="loadData"
      />
    </div>
  </el-card>
  <!--添加、编辑弹出框-->
  <el-dialog v-model="dialogFormVisible" :title="title" width="500" :lock-scroll="false" :close-on-click-modal="false">
    <el-form :model="product">
      <el-form-item label="商品名称" :label-width="80">
        <el-input v-model="product.name" autocomplete="off" />
      </el-form-item>
      <el-form-item label="副标题" :label-width="80">
        <el-input v-model="product.subtitle" autocomplete="off" />
      </el-form-item>
      <el-form-item label="分类ID" :label-width="80">
        <el-input-number v-model="product.categoryId" :min="1" controls-position="right" style="width: 100%"/>
      </el-form-item>
      <el-form-item label="价格(元)" :label-width="80">
        <el-input-number v-model="product.price" :min="0" :precision="2" :step="0.01" controls-position="right" style="width: 100%"/>
      </el-form-item>
      <el-form-item label="库存" :label-width="80">
        <el-input-number v-model="product.stock" :min="0" :step="1" controls-position="right" style="width: 100%"/>
      </el-form-item>
      <el-form-item label="状态" :label-width="80">
        <el-switch
            v-model="product.status"
            :active-value="1"
            :inactive-value="0"
            active-text="在售"
            inactive-text="下架"
        />
      </el-form-item>
      <el-form-item label="主图" :label-width="80">
        <el-upload
            class="image-uploader"
            action="/api/upload"
            :show-file-list="false"
            :on-success="handleMainImageSuccess"
            :headers="{Authorization: tokenStore.token}"
            name="file"
            accept="image/*"
        >
          <img v-if="product.mainImage" :src="product.mainImage" class="image"/>
          <el-icon v-else class="image-uploader-icon">
            <Plus/>
          </el-icon>
        </el-upload>
      </el-form-item>
      <el-form-item label="商品详情" :label-width="80">
        <el-input v-model="product.detail" type="textarea" :rows="4" autocomplete="off" />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogFormVisible = false">取消</el-button>
        <el-button type="primary" @click="addOrUpdate">
          确认
        </el-button>
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

.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.table-image {
  width: 50px;
  height: 50px;
  display: block;
  border-radius: 4px;
  object-fit: cover;
  margin: 0 auto;
}

.image-uploader .image {
  width: 178px;
  height: 178px;
  display: block;
  object-fit: cover;
}

.image-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.image-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}

.el-icon.image-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
}
</style>
