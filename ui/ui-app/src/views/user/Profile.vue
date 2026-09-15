<script setup>
  import {ref, onMounted} from 'vue'
  import {Camera} from '@element-plus/icons-vue'
  import {ElMessage} from 'element-plus'
  import userApi from '@/api/user/user.js'
  import PageHeader from '@/components/PageHeader.vue'
  import {useTokenStore} from '@/store/token.js'
  import {useUserInfoStore} from '@/store/userInfo.js'
  import {formatImage} from '@/utils/format.js'

  const tokenStore = useTokenStore()
  const userInfoStore = useUserInfoStore()

  //个人信息表单
  const user = ref({})
  onMounted(() => {
    userApi.userInfo().then(result => {
      if (result.code === 1) {
        user.value = result.data || {}
      }
    })
  })

  //上传头像成功，回填图片地址
  const handleAvatarSuccess = (result) => {
    user.value.avatar = result.data
  }

  //表单校验模型
  const rules = ref({
    name: [
      {required: true, message: '请输入用户名', trigger: 'blur'},
      {min: 2, max: 16, message: '用户名的长度必须为2~16位', trigger: 'blur'}
    ],
    phone: [
      {pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur'}
    ],
    email: [
      {type: 'email', message: '邮箱格式不正确', trigger: 'blur'}
    ]
  })
  const form = ref()

  //保存个人信息
  const save = (formEl) => {
    formEl.validate((valid) => {
      if (!valid) {
        return
      }
      userApi.update(user.value).then(result => {
        if (result.code === 1) {
          ElMessage.success(result.msg || '保存成功')
          //同步个人中心页面的用户信息
          userInfoStore.setUserInfo(user.value)
        } else {
          ElMessage.error(result.msg || '保存失败')
        }
      })
    })
  }
</script>

<template>
  <div class="profile">
    <PageHeader title="个人信息"/>

    <!-- 头像上传 -->
    <div class="avatar-area">
      <el-upload class="avatar-uploader" action="/api/product/upload" :show-file-list="false"
                 :on-success="handleAvatarSuccess" :headers="{Authorization: tokenStore.token}">
        <img v-if="user.avatar" class="avatar" :src="formatImage(user.avatar)" alt="头像">
        <div v-else class="avatar avatar-default">
          <el-icon :size="30" color="#fff"><Camera/></el-icon>
        </div>
        <span class="avatar-tip">点击更换头像</span>
      </el-upload>
    </div>

    <!-- 资料表单 -->
    <el-form class="profile-form card" ref="form" label-position="top" :model="user" :rules="rules"
             @submit.prevent>
      <el-form-item label="用户名" prop="name">
        <el-input v-model="user.name" placeholder="请输入用户名"/>
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="user.phone" placeholder="请输入手机号" maxlength="11"/>
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="user.email" placeholder="请输入邮箱"/>
      </el-form-item>
      <el-form-item>
        <el-button class="save-button" type="primary" round @click="save(form)">保存</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<style scoped>
  .avatar-area {
    display: flex;
    justify-content: center;
    padding: 24px 0 8px;
  }

  .avatar {
    width: 72px;
    height: 72px;
    border-radius: 50%;
    object-fit: cover;
  }

  .avatar-default {
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #c0c4cc;
  }

  .avatar-tip {
    display: block;
    margin-top: 6px;
    font-size: 12px;
    color: #999;
    text-align: center;
  }

  .profile-form {
    margin: 8px;
    padding: 8px 14px 14px;
  }

  .save-button {
    width: 100%;
    height: 38px;
    font-size: 15px;
    font-weight: 600;
  }
</style>
