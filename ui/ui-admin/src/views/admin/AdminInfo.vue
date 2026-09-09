<script setup>

  import {useAdminInfoStore} from '@/store/adminInfo.js'
  const adminInfoStore = useAdminInfoStore();
  const admin = adminInfoStore.admin;

  const roleList = [
      { value: 0, label: '超级管理员' },
      { value: 1, label: '用户' }
  ]
</script>

<template>
  <el-card class="page-container">
    <template #header>
      <div class="header">
        <span>基本资料</span>
      </div>
    </template>
    <el-descriptions :column="2" border>
      <el-descriptions-item label="用户名">{{ admin.name }}</el-descriptions-item>
      <el-descriptions-item label="手机号">{{ admin.phone }}</el-descriptions-item>
      <el-descriptions-item label="邮箱">{{ admin.email }}</el-descriptions-item>
      <el-descriptions-item label="状态">
        <el-tag :type="admin.status === 1 ? 'success' : 'danger'">
          {{ admin.status === 1 ? '正常' : '禁用' }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="角色">
        <el-tag :type="admin.role === 0 ? 'danger' : 'info'">{{ roleList.find(role => role.value === admin.role)?.label }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="修改时间">{{ admin.updateTime }}</el-descriptions-item>
      <el-descriptions-item label="头像">
        <img :src="admin.avatar" alt="头像" style="width: 120px; height: 150px; border-radius: 50%">
      </el-descriptions-item>
    </el-descriptions>
  </el-card>
</template>

<style lang="scss" scoped>
.page-container {
  .header {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
}
</style>