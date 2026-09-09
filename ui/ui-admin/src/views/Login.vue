<script setup>

  import {useRouter} from 'vue-router'
  import {ref} from "vue";
  import {ElMessage} from "element-plus";

  import adminApi from "@/api/admin/admin.js";
  const router = useRouter()

  import {useTokenStore} from "@/store/token.js";
  import {User} from "@element-plus/icons-vue";
  const tokenStore = useTokenStore()
  const admin = ref({
    name: '',
    password: ''
  })
  //表单引用，用于触发表单校验
  const formRef = ref(null)

  const login = () => {
    //先做表单校验，校验通过才发起登录请求
    formRef.value.validate((valid) => {
      if (!valid) {
        ElMessage.error('请输入用户名或密码')
        return
      }
      adminApi.login(admin.value).then(result => {
        if (result.code === 1) {
          ElMessage.success(result.msg)
          tokenStore.setToken(result.data)
          router.push({path: '/'})
        } else {
          ElMessage.error(result.msg)
        }
      })
    })
  }

  //表单校验模型
  const rules = ref({
    name: [
      {required: true, message: '请输入用户名', trigger: 'blur'},
      {min: 4, max: 16, message: '用户名的长度必须为4~16位', trigger: 'blur'}
    ],
    password: [
      {required: true, message: '请输入密码', trigger: 'blur'},
      {min: 3, max: 16, message: '密码长度必须为3~16位', trigger: 'blur'}
    ]
  })

</script>

<template>
  <div class="login-container">
    <!-- 登录卡片 -->
    <el-form class="form-login" ref="formRef" size="large" autocomplete="off" :model="admin" :rules="rules"
             @keyup.enter="login">
      <div class="login-title">
        <h1>电商管理系统</h1>
      </div>
      <el-form-item prop="name">
        <el-input :prefix-icon="User" placeholder="请输入用户名" v-model="admin.name"></el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input name="password" :prefix-icon="Lock" type="password" placeholder="请输入密码" show-password
                  v-model="admin.password"></el-input>
      </el-form-item>
      <el-form-item>
        <el-button class="button" type="primary" auto-insert-space @click="login">登录</el-button>
      </el-form-item>
    </el-form>
  </div>

</template>

<style scoped>
.login-container {
  height: 100vh;
  overflow: hidden;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  /* 健康暖青渐变背景 */
  background: linear-gradient(135deg, #0f766e 0%, #14b8a6 55%, #5eead4 100%);
}
</style>