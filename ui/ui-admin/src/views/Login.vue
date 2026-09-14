<script setup>

  import {useRouter} from 'vue-router'
  import {ref} from "vue";
  import {ElMessage} from "element-plus";
  import captchaApi from "@/api/service/captcha.js";
  import adminApi from "@/api/admin/admin.js";
  const router = useRouter()

  import {useTokenStore} from "@/store/token.js";
  //Lock 必须在这里显式导入，否则模板里的 :prefix-icon="Lock" 取到的是 undefined，密码框图标不会显示
  import {User, Lock, Shop, Goods, Menu, UserFilled, InfoFilled} from "@element-plus/icons-vue";
  const tokenStore = useTokenStore()
  const loginInfoDTO = ref({
    name: '',
    password: '',
    captcha: '',
    uuid: ''
  })
  //表单引用，用于触发表单校验
  const formRef = ref(null)
  //登录中状态，防止重复点击
  const loading = ref(false)

  //左侧品牌区的能力介绍，和左侧菜单实际拥有的模块保持一致
  const features = [
    {icon: Goods, title: '商品管理', desc: '商品、库存与价格一站式维护'},
    {icon: Menu, title: '分类管理', desc: '多级分类结构清晰可控'},
    {icon: UserFilled, title: '权限管理', desc: '管理员账号与角色统一管理'}
  ]

  //验证码图片地址，加时间戳参数防止浏览器缓存，点击图片刷新
  const captchaSrc = ref('')
  const refreshCaptcha = () => {
    captchaApi.captcha().then(result => {
      captchaSrc.value = result.data.captcha
      loginInfoDTO.value.uuid = result.data.uuid
    })
  }
  refreshCaptcha()

  const login = () => {
    if (loading.value) {
      return
    }
    //先做表单校验，校验通过才发起登录请求
    formRef.value.validate((valid) => {
      if (!valid) {
        ElMessage.error('请输入用户名或密码')
        return
      }
      loading.value = true
      adminApi.login(loginInfoDTO.value).then(result => {
        if (result.code === 1) {
          ElMessage.success(result.msg)
          tokenStore.setToken(result.data)
          router.push({path: '/'})
        } else {
          ElMessage.error(result.msg)
          //验证码是一次性的，登录失败后刷新图片重新获取
          refreshCaptcha()
        }
      }).finally(() => {
        //无论成功失败都要恢复按钮状态
        loading.value = false
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
  <div class="login-page">
    <!-- 背景装饰：光斑 + 网格，纯装饰，不参与交互 -->
    <div class="bg-decoration">
      <span class="blob blob-1"></span>
      <span class="blob blob-2"></span>
      <span class="blob blob-3"></span>
      <div class="grid-mask"></div>
    </div>

    <!-- 登录卡片：左侧品牌区 + 右侧表单区 -->
    <div class="login-card">
      <aside class="brand-panel">
        <div class="brand-logo">
          <el-icon>
            <Shop/>
          </el-icon>
          <span>Mall Admin</span>
        </div>
        <h1 class="brand-title">电商管理系统</h1>
        <p class="brand-subtitle">E-COMMERCE ADMIN SYSTEM</p>

        <ul class="brand-features">
          <li v-for="item in features" :key="item.title">
            <el-icon class="feature-icon">
              <component :is="item.icon"/>
            </el-icon>
            <div class="feature-text">
              <p class="feature-title">{{ item.title }}</p>
              <p class="feature-desc">{{ item.desc }}</p>
            </div>
          </li>
        </ul>

        <p class="brand-footer">让每一次经营决策都有数据支撑</p>
      </aside>

      <section class="form-panel">
        <div class="form-header">
          <h2>账号登录</h2>
          <p>欢迎回来，请输入管理员账号信息</p>
        </div>

        <el-form class="form-login" ref="formRef" size="large" autocomplete="off" :model="loginInfoDTO" :rules="rules"
                 @keyup.enter="login">
          <el-form-item prop="name">
            <el-input :prefix-icon="User" placeholder="请输入用户名" v-model="loginInfoDTO.name" clearable></el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input name="password" :prefix-icon="Lock" type="password" placeholder="请输入密码" show-password
                      v-model="loginInfoDTO.password"></el-input>
          </el-form-item>
          <el-form-item prop="captcha">
            <div class="captcha-row">
              <el-input :prefix-icon="Key" placeholder="请输入验证码" v-model="loginInfoDTO.captcha"
                        @keyup.enter="login"></el-input>
              <img class="captcha-img" :src="captchaSrc" alt="验证码" title="点击刷新" @click="refreshCaptcha">
            </div>
          </el-form-item>
          <el-form-item class="form-item-submit">
            <el-button class="login-button" type="primary" auto-insert-space :loading="loading" @click="login">登录
            </el-button>
          </el-form-item>
        </el-form>

        <p class="form-tip">
          <el-icon>
            <InfoFilled/>
          </el-icon>
          仅限管理员登录，普通用户请前往前台商城
        </p>
      </section>
    </div>

    <footer class="login-footer">后台管理 ©2024 Created by Gao</footer>
  </div>
</template>

<style lang="scss" scoped>
.login-page {
  position: relative;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32px 20px;
  box-sizing: border-box;
  /* 品牌墨绿→青色渐变，与后台整体色系保持一致 */
  background: linear-gradient(135deg, #08312f 0%, #0f766e 52%, #14b8a6 100%);
}

.bg-decoration {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;

  .blob {
    position: absolute;
    border-radius: 50%;
    filter: blur(60px);
    opacity: .45;
  }

  .blob-1 {
    width: 420px;
    height: 420px;
    top: -140px;
    left: -120px;
    background: #5eead4;
  }

  .blob-2 {
    width: 360px;
    height: 360px;
    right: -90px;
    bottom: -130px;
    background: #22d3ee;
    opacity: .32;
  }

  /* 一点暖黄，呼应左侧菜单的 #ffd04b 选中色 */
  .blob-3 {
    width: 260px;
    height: 260px;
    top: 42%;
    right: 20%;
    background: #fbbf24;
    opacity: .16;
  }

  .grid-mask {
    position: absolute;
    inset: 0;
    background-image: linear-gradient(rgba(255, 255, 255, .16) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, .16) 1px, transparent 1px);
    background-size: 44px 44px;
    -webkit-mask-image: radial-gradient(circle at 50% 38%, #000 0%, transparent 72%);
    mask-image: radial-gradient(circle at 50% 38%, #000 0%, transparent 72%);
  }
}

.login-card {
  position: relative;
  z-index: 1;
  display: flex;
  width: 100%;
  max-width: 940px;
  min-height: 522px;
  border-radius: 20px;
  overflow: hidden;
  background: #fff;
  box-shadow: 0 24px 60px rgba(3, 42, 40, .38);
}

/* 左侧品牌区 */
.brand-panel {
  position: relative;
  width: 46%;
  display: flex;
  flex-direction: column;
  padding: 44px 38px;
  box-sizing: border-box;
  color: #e6fffb;
  background: linear-gradient(160deg, #0f766e 0%, #115e59 55%, #0f3f3c 100%);

  &::after {
    content: '';
    position: absolute;
    top: -90px;
    right: -90px;
    width: 250px;
    height: 250px;
    border-radius: 50%;
    background: radial-gradient(circle, rgba(94, 234, 212, .45) 0%, transparent 68%);
  }

  .brand-logo {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 17px;
    font-weight: 600;
    letter-spacing: .6px;
    color: #fff;

    .el-icon {
      font-size: 26px;
      color: #5eead4;
    }
  }

  .brand-title {
    margin: 42px 0 10px;
    font-size: 30px;
    font-weight: 700;
    letter-spacing: 3px;
    color: #fff;
  }

  .brand-subtitle {
    margin: 0 0 38px;
    font-size: 11px;
    letter-spacing: 2px;
    color: rgba(255, 255, 255, .58);
  }

  .brand-features {
    margin: 0;
    padding: 0;
    list-style: none;
    display: flex;
    flex-direction: column;
    gap: 20px;

    li {
      display: flex;
      align-items: flex-start;
      gap: 12px;
    }

    .feature-icon {
      flex: none;
      width: 34px;
      height: 34px;
      border-radius: 10px;
      font-size: 17px;
      color: #5eead4;
      background: rgba(94, 234, 212, .14);
      border: 1px solid rgba(94, 234, 212, .26);
    }

    .feature-text {
      .feature-title {
        margin: 0;
        font-size: 14px;
        font-weight: 600;
        color: #fff;
      }

      .feature-desc {
        margin: 4px 0 0;
        font-size: 12px;
        line-height: 1.5;
        color: rgba(255, 255, 255, .6);
      }
    }
  }

  .brand-footer {
    margin: auto 0 0;
    padding-top: 24px;
    font-size: 12px;
    letter-spacing: .5px;
    color: rgba(255, 255, 255, .55);
  }
}

/* 右侧表单区 */
.form-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 54px 48px 44px;
  box-sizing: border-box;

  .form-header {
    margin-bottom: 30px;

    h2 {
      margin: 0 0 8px;
      font-size: 24px;
      font-weight: 600;
      color: #1f2d3d;
    }

    p {
      margin: 0;
      font-size: 13px;
      color: #93a0ac;
    }
  }

  .form-tip {
    display: flex;
    align-items: center;
    gap: 6px;
    margin: 22px 0 0;
    font-size: 12px;
    color: #93a0ac;

    .el-icon {
      font-size: 14px;
      color: #14b8a6;
    }
  }
}

.form-login {
  :deep(.el-form-item) {
    margin-bottom: 22px;
  }

  /* 提交按钮那一项不需要下间距 */
  :deep(.form-item-submit) {
    margin-bottom: 0;
  }

  :deep(.el-input__wrapper) {
    padding: 4px 14px;
    border-radius: 10px;
    background-color: #f6f8f9;
    box-shadow: 0 0 0 1px #e3eaee inset;
    transition: background-color .2s, box-shadow .2s;

    &:hover {
      box-shadow: 0 0 0 1px #b7d3d0 inset;
    }

    &.is-focus,
    &.is-focus:hover {
      background-color: #fff;
      box-shadow: 0 0 0 1px #14b8a6 inset, 0 0 0 4px rgba(20, 184, 166, .12);
    }
  }

  :deep(.el-input__prefix),
  :deep(.el-input__suffix) {
    color: #9aa7b1;
  }
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

.login-button {
  width: 100%;
  height: 46px;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 2px;
  background: linear-gradient(135deg, #0f766e 0%, #14b8a6 100%);
  box-shadow: 0 10px 20px rgba(15, 118, 110, .26);
  transition: transform .2s, box-shadow .2s, filter .2s;

  &:hover {
    filter: brightness(1.06);
    transform: translateY(-1px);
    box-shadow: 0 13px 26px rgba(15, 118, 110, .33);
  }

  &:active {
    transform: translateY(0);
  }
}

.login-footer {
  position: relative;
  z-index: 1;
  margin-top: 26px;
  font-size: 12px;
  letter-spacing: .5px;
  color: rgba(255, 255, 255, .66);
}

/* 小屏幕：卡片改为上下结构，品牌区收缩成顶部条 */
@media (max-width: 900px) {
  .login-card {
    flex-direction: column;
    max-width: 460px;
    min-height: auto;
  }

  .brand-panel {
    width: 100%;
    padding: 26px 28px;

    .brand-title {
      margin: 14px 0 0;
      font-size: 22px;
      letter-spacing: 2px;
    }

    .brand-subtitle {
      margin: 6px 0 0;
    }

    .brand-features,
    .brand-footer {
      display: none;
    }
  }

  .form-panel {
    padding: 30px 28px 34px;
  }
}
</style>
