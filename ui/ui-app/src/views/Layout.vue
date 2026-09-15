<script setup>
  import TabBar from '@/components/TabBar.vue'
  import {useRoute} from 'vue-router'

  const route = useRoute()
</script>

<template>
  <div class="app-layout">
    <!-- 内容区域 -->
    <router-view></router-view>
    <!-- 底部TabBar只在四个标签页显示，子页面隐藏 -->
    <TabBar v-if="route.meta.tabbar"></TabBar>
  </div>
</template>

<style scoped>
  .app-layout {
    min-height: 100vh;
  }
</style>
