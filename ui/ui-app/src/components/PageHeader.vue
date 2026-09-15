<script setup>
  import {useRouter} from 'vue-router'
  import {ArrowLeft} from '@element-plus/icons-vue'

  //props：title标题，back是否显示返回按钮
  const props = defineProps({
    title: String,
    back: {
      type: Boolean,
      default: true
    }
  })

  const router = useRouter()
  const goBack = () => router.back()
</script>

<template>
  <!-- 吸顶的页面头部：返回按钮 + 居中标题 + 右侧插槽 -->
  <header class="page-header">
    <div v-if="back" class="header-back" @click="goBack">
      <el-icon :size="20"><ArrowLeft/></el-icon>
    </div>
    <div class="header-title ellipsis-1">{{ title }}</div>
    <div class="header-right">
      <slot name="right"></slot>
    </div>
  </header>
</template>

<style scoped>
  .page-header {
    position: sticky;
    top: 0;
    z-index: 100;
    display: flex;
    align-items: center;
    justify-content: center;
    height: 44px;
    background-color: #fff;
    border-bottom: 1px solid #f0f0f0;
  }

  .header-back {
    position: absolute;
    left: 0;
    top: 0;
    width: 44px;
    height: 44px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #333;
  }

  .header-title {
    max-width: 60%;
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }

  .header-right {
    position: absolute;
    right: 0;
    top: 0;
    height: 44px;
    display: flex;
    align-items: center;
    padding-right: 12px;
    font-size: 14px;
    color: #303133;
  }
</style>
