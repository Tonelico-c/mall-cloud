<script setup>
  import {ref, onMounted, onUnmounted, watch} from 'vue'
  import {useRoute} from 'vue-router'
  import {Loading} from '@element-plus/icons-vue'
  import productApi from '@/api/product/product.js'
  import PageHeader from '@/components/PageHeader.vue'
  import ProductCard from '@/components/ProductCard.vue'

  const route = useRoute()

  //列表数据
  const list = ref([])
  const total = ref(0)
  //分页信息和搜索条件
  const productQuery = ref({
    name: '',
    categoryId: null,
    page: 1,
    limit: 10
  })
  const loading = ref(false)

  //页面标题：搜索关键字或分类名称
  const title = ref('商品列表')

  const loadData = () => {
    loading.value = true
    productApi.list(productQuery.value).then(result => {
      if (result.code === 1) {
        //只展示在售商品，追加到列表后面实现无限加载
        const records = (result.data.records || []).filter(item => item.status === 1)
        list.value = [...list.value, ...records]
        total.value = result.data.total
      }
      loading.value = false
    }).catch(() => {
      loading.value = false
    })
  }

  //重置查询条件并重新加载（从首页再次搜索时路由query变化）
  const reload = () => {
    productQuery.value.name = route.query.name || ''
    productQuery.value.categoryId = route.query.categoryId || null
    productQuery.value.page = 1
    list.value = []
    total.value = 0
    //分类页跳转时用title指定标题；name是商品名搜索关键字，不能既当标题又当过滤条件
    title.value = route.query.title || (route.query.name ? '搜索：' + route.query.name : '商品列表')
    loadData()
  }
  reload()
  watch(() => route.query, reload)

  //是否还有更多数据
  const hasMore = () => list.value.length < total.value

  //加载下一页
  const loadMore = () => {
    if (loading.value || !hasMore()) {
      return
    }
    productQuery.value.page++
    loadData()
  }

  //滚动到底部自动加载下一页
  const sentinel = ref()
  let observer = null
  onMounted(() => {
    observer = new IntersectionObserver((entries) => {
      if (entries[0].isIntersecting) {
        loadMore()
      }
    })
    observer.observe(sentinel.value)
  })
  onUnmounted(() => {
    observer?.disconnect()
  })
</script>

<template>
  <div class="product-list pb-action">
    <!-- 页面头部：标题为搜索关键字或分类名称 -->
    <PageHeader :title="title"/>

    <!-- 商品两列宫格 -->
    <div v-if="list.length > 0 || loading" class="product-grid">
      <ProductCard v-for="product in list" :key="product.id" :product="product"/>
    </div>

    <!-- 空状态 -->
    <el-empty v-if="!loading && list.length === 0" description="暂无相关商品"/>

    <!-- 底部：加载中 / 没有更多 / 触底自动加载的哨兵元素
         必须用v-show：挂载时列表还是空的，v-if会让元素不存在，onMounted里observe(undefined)抛异常，Vue调度器会被卡死，页面不再渲染 -->
    <div v-show="list.length > 0" class="list-loading" ref="sentinel">
      <template v-if="loading">
        <el-icon class="is-loading"><Loading/></el-icon>
        <span>加载中...</span>
      </template>
      <span v-else-if="!hasMore()" class="list-finished">— 没有更多了 —</span>
    </div>
  </div>
</template>

<style scoped>
  .product-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 8px;
    padding: 8px;
  }
</style>
