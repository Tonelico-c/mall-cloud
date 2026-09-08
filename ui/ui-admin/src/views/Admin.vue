<script setup>
  import adminApi from '@/api/admin.js'
  import {ref} from 'vue'
  import {ElMessage, ElMessageBox} from 'element-plus'
  import {Delete, Edit, Plus, Search, Refresh} from '@element-plus/icons-vue'

  //表格数据
  const list = ref([])
  const total = ref(0)
  //分页信息和搜索条件
  const adminQuery = ref({
    name: '',
    email: '',
    page: 1,
    limit: 10
  })

  // 时间范围
  const createTimeRange = ref([])
  const loadData = () => {
    adminQuery.value.beginCreateTime = createTimeRange.value?.[0];
    adminQuery.value.endCreateTime = createTimeRange.value?.[1];
    adminApi.list(adminQuery.value).then(result => {
      list.value = result.data.records
      total.value = result.data.total
    })
  }

  loadData()

  const onSearch = () => {
    adminQuery.value.page = 1
    loadData()
  }
  const resetSearch = () => {
    createTimeRange.value = []
    adminQuery.value = {
      name: '',
      email: '',
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
      adminApi.deleteById(id).then(result => {
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
      adminApi.deleteAll(ids).then(result => {
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
  const admin = ref({})
  const title = ref()

  const showAddDialog = () => {
    dialogFormVisible.value = true
    title.value = '添加'
    admin.value = {}
  }

  const showUpdateDialog = (id) => {
    dialogFormVisible.value = true
    title.value = '编辑'
    admin.value = {}
    adminApi.selectById(id).then(result => {
      admin.value = result.data
    })
  }

  const addOrUpdate = () => {
    if (admin.value.id) {//编辑
      adminApi.update(admin.value.id, admin.value).then(result => {
        if (result.code === 1) {
          ElMessage.success(result.msg)
          dialogFormVisible.value = false
          loadData()
        } else {
          ElMessage.error(result.msg)
        }
      })
    } else {//添加
      adminApi.add(admin.value).then(result => {
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

  // 状态切换
  const handleSwitchChange = (row) => {
    const data = {}
    data.id = row.id
    data.status = row.status
    adminApi.update(data.id, data).then(result => {
      if (result.code === 1) {
        ElMessage.success(result.msg)
        loadData()
      } else {
        ElMessage.error(result.msg)
      }
    })
  }

  // 角色显示
  const roleText = (role) => (role === 0 ? '管理员' : '普通用户')
</script>

<template>
  <el-card class="">
    <template #header>
      <el-form :inline="true" class="search-form" @submit.prevent>
        <el-form-item label="用户名">
          <el-input
              v-model="adminQuery.name"
              placeholder="请输入用户名"
              clearable
              style="width: 220px"
              @keyup.enter="onSearch"
          />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input
              v-model="adminQuery.email"
              placeholder="请输入邮箱"
              clearable
              style="width: 220px"
              @keyup.enter="onSearch"
          />
        </el-form-item>
        <el-form-item label="创建时间">
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
    <div class="toolbar">
      <el-button type="primary" :icon="Plus" @click="showAddDialog">添加</el-button>
      <el-button type="danger" :icon="Delete" @click="deleteAll">批量删除</el-button>
    </div>
    <el-table :data="list" border style="width: 100%" show-overflow-tooltip @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" />
      <el-table-column fixed prop="id" label="ID" width="80"/>
      <el-table-column prop="avatar" label="头像" width="100">
        <template #default="{row}">
          <img :src="row.avatar" alt="头像" class="table-avatar" v-if="row.avatar"/>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="用户名" width="150"/>
      <el-table-column prop="email" label="邮箱" width="200"/>
      <el-table-column prop="phone" label="电话" width="150"/>
      <el-table-column prop="role" label="角色" width="100">
        <template #default="{row}">
          <el-tag :type="row.role === 0 ? 'danger' : 'info'">{{ roleText(row.role) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态">
        <template #default="{row}">
          <el-switch
              v-model="row.status"
              style="--el-switch-on-color: #13ce66; --el-switch-off-color: #ff4949"
              :active-value="1"
              :inactive-value="0"
              inline-prompt
              active-text="正常"
              inactive-text="停用"
              @change="handleSwitchChange(row)"
          />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="200"/>
      <el-table-column align="center" width="200" fixed="right" labe="操作">
        <template #default="{ row }">
          <el-button size="small" type="primary" :icon="Edit" @click="showUpdateDialog(row.id)">编辑</el-button>
          <el-button size="small" type="danger" :icon="Delete" @click="deleteById(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination-wrapper">
      <el-pagination
          v-model:current-page="adminQuery.page"
          v-model:page-size="adminQuery.limit"
          :page-sizes="[10, 20, 30, 40]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @change="loadData"
      />
    </div>
  </el-card>
  <!--添加、编辑弹出框-->
  <el-dialog v-model="dialogFormVisible" :title="title" width="500" :lock-scroll="false" :close-on-click-modal="false">
    <el-form :model="admin">
      <el-form-item label="用户名" :label-width="80">
        <el-input v-model="admin.name" autocomplete="off" />
      </el-form-item>
      <el-form-item label="密码" :label-width="80">
        <el-input v-model="admin.password" type="password" show-password
                  :placeholder="admin.id ? '不修改请留空' : '请输入密码'" autocomplete="new-password" />
      </el-form-item>
      <el-form-item label="邮箱" :label-width="80">
        <el-input v-model="admin.email" autocomplete="off" />
      </el-form-item>
      <el-form-item label="手机号" :label-width="80">
        <el-input v-model="admin.phone" autocomplete="off" />
      </el-form-item>
      <el-form-item label="角色" :label-width="80">
        <el-select v-model="admin.role" placeholder="请选择角色" style="width: 100%">
          <el-option label="管理员" :value="0" />
          <el-option label="普通用户" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" :label-width="80">
        <el-switch
            v-model="admin.status"
            :active-value="1"
            :inactive-value="0"
            active-text="正常"
            inactive-text="停用"
        />
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

.table-avatar {
  width: 50px;
  height: 50px;
  display: block;
  border-radius: 50%;
  object-fit: cover;
  margin: 0 auto;
}
</style>
