<script setup>
  //定义数据模型
  import {ref} from "vue";
  import {User, Lock, ShoppingCart} from "@element-plus/icons-vue";
  import userApi from "@/api/user/user.js";
  import {ElMessage} from "element-plus";
  import {useRoute, useRouter} from 'vue-router'
  const route = useRoute()
  const router = useRouter()
  import {useTokenStore} from '@/store/token.js'
  import {useCartCountStore} from '@/store/cartCount.js'
  import captchaApi from "@/api/service/captcha.js";
  const tokenStore = useTokenStore();
  const cartCountStore = useCartCountStore()

  const loginInfo = ref({
    name: '',
    password: '',
    captcha: '',
    uuid: ''
  })
  //验证码图片地址，加时间戳参数防止浏览器缓存，点击图片刷新
  const captchaSrc = ref('')
  const refreshCaptcha = () => {
    captchaApi.captcha().then(result => {
      captchaSrc.value = result.data.captcha
      loginInfo.value.uuid = result.data.uuid
    })
  }

  refreshCaptcha()

  const login = () => {
    userApi.login(loginInfo.value).then(result => {
      if (result.code === 1) {
        ElMessage.success(result.msg || '登录成功')
        tokenStore.setToken(result.data)
        //登录后刷新购物车角标
        cartCountStore.refreshCount()
        //跳回原本想访问的页面，没有则去首页
        router.push(route.query.redirect || '/')
      } else {
        ElMessage.error(result.msg || '登录失败')
        refreshCaptcha()
      }
    })
  }

  //表单校验模型
  const rules = ref({
    name: [
      {required: true, message: '请输入用户名', trigger: 'blur'},
      {min: 2, max: 16, message: '用户名的长度必须为2~16位', trigger: 'blur'}
    ],
    password: [
      {required: true, message: '请输入密码', trigger: 'blur'},
      {min: 3, max: 16, message: '密码长度必须为3~16位', trigger: 'blur'}
    ]
  })
  const form = ref()
  const submit = (formEl) => {
    formEl.validate((valid) => {
      if (valid) {
        login()
      }
    })
  }
</script>

<template>
  <div class="login">
    <!-- 商城Logo -->
    <div class="login-logo">
      <el-icon :size="36" color="#fff"><ShoppingCart/></el-icon>
    </div>
    <h1 class="login-title">思途商城</h1>
    <p class="login-subtitle">品质好物 · 极速送达</p>

    <!-- 登录表单 -->
    <el-form class="login-form" ref="form" size="large" autocomplete="off"
             :model="loginInfo" :rules="rules" @submit.prevent>
      <el-form-item prop="name">
        <el-input :prefix-icon="User" placeholder="请输入用户名" v-model="loginInfo.name"></el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input name="password" :prefix-icon="Lock" type="password" placeholder="请输入密码"
                  v-model="loginInfo.password" @keyup.enter="submit(form)"></el-input>
      </el-form-item>
      <el-form-item prop="captcha">
        <div class="captcha-row">
          <el-input :prefix-icon="Key" placeholder="请输入验证码" v-model="loginInfo.captcha"
                    @keyup.enter="login"></el-input>
          <img class="captcha-img" :src="captchaSrc" alt="验证码" title="点击刷新" @click="refreshCaptcha">
        </div>
      </el-form-item>
      <el-form-item>
        <el-button class="login-button" type="primary" auto-insert-space @click="submit(form)">登录</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<style scoped>
  .login {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding-top: 80px;
    background: linear-gradient(180deg, #fff 0%, #fdf0f0 100%);
  }

  .login-logo {
    width: 72px;
    height: 72px;
    border-radius: 50%;
    background: linear-gradient(135deg, #ff6034, var(--el-color-primary));
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 8px 20px rgba(228, 57, 60, 0.3);
  }

  .login-title {
    margin: 16px 0 4px;
    font-size: 22px;
    color: #303133;
  }

  .login-subtitle {
    margin: 0 0 32px;
    font-size: 13px;
    color: #999;
  }

  .login-form {
    width: calc(100% - 64px);
  }

  .login-button {
    width: 100%;
    border-radius: 22px;
  }
  /* 验证码行：输入框与验证码图片同行 */
  .captcha-row {
    display: flex;
    align-items: center;
    gap: 12px;
    width: 100%;

    .el-input {
      flex: 1;
    }

    .captcha-img {
      flex: none;
      height: 40px;
      min-width: 110px;
      border-radius: 10px;
      border: 1px solid #e3eaee;
      background-color: #fff;
      object-fit: contain;
      cursor: pointer;
      transition: border-color .2s;

      &:hover {
        border-color: #14b8a6;
      }
    }
  }
</style>
