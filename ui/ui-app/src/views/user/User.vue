<script setup>
  import {computed, onMounted} from 'vue'
  import {useRouter} from 'vue-router'
  import {User, Location, Tickets, SwitchButton, ArrowRight} from '@element-plus/icons-vue'
  import {ElMessage, ElMessageBox} from 'element-plus'
  import userApi from '@/api/user/user.js'
  import {useTokenStore} from '@/store/token.js'
  import {useUserInfoStore} from '@/store/userInfo.js'
  import {useCartCountStore} from '@/store/cartCount.js'
  import {formatImage} from '@/utils/format.js'

  const router = useRouter()
  const tokenStore = useTokenStore()
  const userInfoStore = useUserInfoStore()
  const cartCountStore = useCartCountStore()

  //用户信息（store持久化，进入页面时再刷新一次）
  const user = computed(() => userInfoStore.user || {})

  onMounted(() => {
    userApi.userInfo().then(result => {
      if (result.code === 1) {
        userInfoStore.setUserInfo(result.data)
      }
    })
  })

  //菜单配置
  const menus = [
    {title: '个人信息', icon: User, path: '/user/profile'},
    {title: '收货地址', icon: Location, path: '/shipping'},
    {title: '我的订单', icon: Tickets, path: '/order'}
  ]

  //退出登录
  const logout = () => {
    ElMessageBox.confirm('确认退出登录吗？', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning',
      lockScroll: false //防止抖动
    }).then(() => {
      tokenStore.removeToken()
      userInfoStore.removeUserInfo()
      cartCountStore.clear()
      ElMessage.success('已退出登录')
      router.push('/login')
    }).catch(() => {
    })
  }
</script>

<template>
  <div class="user pb-tab">
    <!-- 用户信息卡片 -->
    <div class="user-card">
      <img v-if="user.avatar" class="user-avatar" :src="formatImage(user.avatar)" alt="头像">
      <div v-else class="user-avatar user-avatar-default">
        <el-icon :size="30" color="#fff"><User/></el-icon>
      </div>
      <div class="user-info">
        <div class="user-name">{{ user.name || '商城用户' }}</div>
        <div class="user-contact">
          <span v-if="user.phone">{{ user.phone }}</span>
          <span v-if="user.phone && user.email"> | </span>
          <span v-if="user.email">{{ user.email }}</span>
          <span v-if="!user.phone && !user.email">点击下方编辑个人信息</span>
        </div>
      </div>
    </div>

    <!-- 功能菜单 -->
    <div class="menu-card card">
      <div v-for="menu in menus" :key="menu.path" class="menu-item" @click="router.push(menu.path)">
        <el-icon class="menu-icon" :size="18"><component :is="menu.icon"/></el-icon>
        <span class="menu-title">{{ menu.title }}</span>
        <el-icon class="menu-arrow"><ArrowRight/></el-icon>
      </div>
    </div>

    <!-- 退出登录 -->
    <div class="logout-card card" @click="logout">
      <el-icon :size="18"><SwitchButton/></el-icon>
      <span>退出登录</span>
    </div>

    <p class="copyright">思途商城 ©2026</p>
  </div>
</template>

<style scoped>
  .user-card {
    display: flex;
    align-items: center;
    gap: 14px;
    padding: 28px 16px 24px;
    background: linear-gradient(135deg, var(--el-color-primary-light-5) 0%, #fff 80%);
  }

  .user-avatar {
    width: 56px;
    height: 56px;
    border-radius: 50%;
    object-fit: cover;
    flex-shrink: 0;
  }

  .user-avatar-default {
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #ff6034, var(--el-color-primary));
  }

  .user-name {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
  }

  .user-contact {
    margin-top: 4px;
    font-size: 12px;
    color: #999;
  }

  .menu-card {
    margin: 8px;
  }

  .menu-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 15px 14px;
    border-bottom: 1px solid #f5f5f5;
  }

  .menu-item:last-child {
    border-bottom: none;
  }

  .menu-icon {
    color: var(--el-color-primary);
  }

  .menu-title {
    font-size: 15px;
    color: #303133;
  }

  .menu-arrow {
    margin-left: auto;
    color: #c0c4cc;
  }

  .logout-card {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    margin: 8px;
    padding: 15px 0;
    font-size: 15px;
    color: var(--el-color-primary);
  }

  .copyright {
    margin-top: 24px;
    text-align: center;
    font-size: 11px;
    color: #c0c4cc;
  }
</style>
