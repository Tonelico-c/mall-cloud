<script setup>
  import categoryApi from '@/api/category/category.js'
  import {ref, reactive} from 'vue'
  import {ElMessage, ElMessageBox} from "element-plus";
  import adminApi from "@/api/admin/admin.js";
  //保存返回树形结构数据，但是本质上List<CategoryVO> list;
  const list = ref([])

  const loadData = () => {
    categoryApi.tree().then(result => {
      list.value = result.data
    })
  }
  //加载数据
  loadData()

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
      categoryApi.deleteById(id).then(result => {
        if (result.code === 1) {
          ElMessage({message: result.msg, type: 'success',})
        } else {
          ElMessage.error(result.msg)
        }
        loadData()
      })
    })
  }

  //添加、编辑
  const dialogFormVisible = ref(false)
  const category = ref({})
  const title = ref()

  const showAddDialog = (id) => {
    dialogFormVisible.value = true
    title.value = '添加'
    category.value = {}
    category.value.parentId = id
  }

  const showUpdateDialog = (id) => {
    dialogFormVisible.value = true
    title.value = '编辑'
    category.value = {}
    categoryApi.selectById(id).then(result => {
      category.value = result.data
    })
  }

  const addOrUpdate = () => {
    if (category.value.id) {//编辑
      categoryApi.update(category.value.id, category.value).then(result => {
        if (result.code === 1) {
          ElMessage({message: result.msg, type: 'success',})
          dialogFormVisible.value = false
        } else {
          ElMessage.error(result.msg)
        }
        loadData()
      })
    } else {//添加
      categoryApi.add(category.value).then(result => {
        if (result.code === 1) {
          ElMessage({message: result.msg, type: 'success',})
          dialogFormVisible.value = false
        } else {
          ElMessage.error(result.msg)
        }
        loadData()
      })
    }
  }
</script>

<template>
  <el-button type="success" @click="showAddDialog(0)">添加顶级分类</el-button>
  <el-table
      :data="list"
      style="width: 100%; margin-bottom: 20px"
      row-key="id"
      border
  >
    <el-table-column prop="name" label="分类名称"/>
    <el-table-column prop="sort" label="排序"/>
    <el-table-column prop="createTime" label="创建时间"/>
    <el-table-column label="操作" align="center" fixed="right"  #default="scope">
      <el-button
          type="success"
          @click="showAddDialog(scope.row.id)"
      >添加</el-button>
      <el-button
          type="primary"
          @click="showUpdateDialog(scope.row.id)"
      >修改</el-button>
      <el-button
          type="danger"
          @click="deleteById(scope.row.id)"
          :disabled="scope.row.children?.length > 0"
      >删除</el-button>
    </el-table-column>
  </el-table>

  <!--添加、编辑弹出框-->
  <el-dialog v-model="dialogFormVisible" :title="title" width="500" :lock-scroll="false">
    <el-form :model="category">
      <el-form-item label="名字" :label-width="60">
        <el-input v-model="category.name" autocomplete="off"/>
      </el-form-item>
      <el-form-item label="排序" :label-width="60">
        <el-input-number v-model="category.sort" :min="0" autocomplete="off"/>
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
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}
</style>