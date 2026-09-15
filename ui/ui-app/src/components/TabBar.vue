<script setup>
  import {useRoute, useRouter} from 'vue-router'
  import {HomeFilled, Menu, ShoppingCart, User} from '@element-plus/icons-vue'
  import {useCartCountStore} from '@/store/cartCount.js'

  const route = useRoute()
  const router = useRouter()
  const cartCountStore = useCartCountStore()

  //底部导航的四个标签页
  const tabs = [
    {path: '/home', title: '首页', icon: HomeFilled},
    {path: '/category', title: '分类', icon: Menu},
    {path: '/cart', title: '购物车', icon: ShoppingCart},
    {path: '/user', title: '我的', icon: User}
  ]

  const isActive = (path) => route.path === path
  const to = (path) => router.push(path)
</script>

<template>
  <nav class="tab-bar app-fixed">
    <div v-for="tab in tabs" :key="tab.path" class="tab-item" :class="{active: isActive(tab.path)}"
         @click="to(tab.path)">
      <div class="tab-icon">
        <el-icon :size="22"><component :is="tab.icon"/></el-icon>
        <!-- 购物车角标：显示购物车商品总数量 -->
        <span v-if="tab.path === '/cart' && cartCountStore.count > 0" class="tab-badge">
          {{ cartCountStore.count > 99 ? '99+' : cartCountStore.count }}
        </span>
      </div>
      <span class="tab-title">{{ tab.title }}</span>
    </div>
  </nav>
</template>

<style scoped>
  .tab-bar {
    bottom: 0;
    display: flex;
    background-color: #fff;
    border-top: 1px solid #f0f0f0;
    /*适配iPhone底部安全区*/
    padding-bottom: env(safe-area-inset-bottom);
    z-index: 100;
  }

  .tab-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 50px;
    color: #666;
  }

  .tab-item.active {
    color: var(--el-color-primary);
  }

  .tab-icon {
    position: relative;
    line-height: 1;
  }

  .tab-title {
    font-size: 10px;
    margin-top: 2px;
  }

  .tab-badge {
    position: absolute;
    top: -5px;
    left: 14px;
    min-width: 16px;
    height: 16px;
    line-height: 16px;
    padding: 0 4px;
    box-sizing: border-box;
    background-color: var(--el-color-primary);
    color: #fff;
    font-size: 10px;
    text-align: center;
    border-radius: 8px;
  }
</style>
