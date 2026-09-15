<script setup>
  import {ref} from 'vue'
  import {useRouter} from 'vue-router'
  import {Search} from '@element-plus/icons-vue'
  import {ElMessage} from 'element-plus'
  import categoryApi from '@/api/category/category.js'
  import productApi from '@/api/product/product.js'
  import ProductCard from '@/components/ProductCard.vue'

  const router = useRouter()

  //搜索关键字
  const keyword = ref('')
  const onSearch = () => {
    if (!keyword.value.trim()) {
      ElMessage.warning('请输入要搜索的商品名称')
      return
    }
    router.push({path: '/product', query: {name: keyword.value.trim()}})
  }

  //一级分类宫格
  const categoryList = ref([])
  categoryApi.tree().then(result => {
    if (result.code === 1) {
      //tree接口返回的就是一级分类数组，children是二级分类
      categoryList.value = result.data || []
    }
  })

  //点击一级分类，跳到分类页并定位到该分类
  const toCategory = (category) => {
    router.push({path: '/category', query: {id: category.id}})
  }

  //新品推荐，取第一页商品（按创建时间倒序）
  const productList = ref([])
  productApi.list({page: 1, limit: 10}).then(result => {
    if (result.code === 1) {
      //只展示在售商品
      productList.value = (result.data.records || []).filter(item => item.status === 1)
    }
  })
</script>

<template>
  <div class="home pb-tab">
    <!-- 搜索栏 -->
    <div class="home-search">
      <el-input v-model="keyword" placeholder="搜索商品" clearable :prefix-icon="Search"
                @keyup.enter="onSearch"/>
      <el-button type="primary" round @click="onSearch">搜索</el-button>
    </div>

    <!-- 分类宫格 -->
    <div class="section">
      <div class="section-title">商品分类</div>
      <div class="cat-grid">
        <div v-for="category in categoryList" :key="category.id" class="cat-item"
             @click="toCategory(category)">
          <div class="cat-name ellipsis-1">{{ category.name }}</div>
          <div class="cat-count">{{ (category.children || []).length }} 个分类</div>
        </div>
      </div>
    </div>

    <!-- 新品推荐 -->
    <div class="section">
      <div class="section-title">新品推荐</div>
      <div class="product-grid">
        <ProductCard v-for="product in productList" :key="product.id" :product="product"/>
      </div>
    </div>
  </div>
</template>

<style scoped>
  .home-search {
    display: flex;
    gap: 8px;
    padding: 8px 12px;
    background: linear-gradient(180deg, var(--el-color-primary) 0%, var(--el-color-primary-light-5) 100%);
  }

  .section {
    margin: 10px 8px 0;
  }

  .section-title {
    display: flex;
    align-items: center;
    padding: 8px 4px;
    font-size: 15px;
    font-weight: 600;
    color: #303133;
  }

  /*标题前的红色装饰条*/
  .section-title::before {
    content: '';
    width: 4px;
    height: 16px;
    margin-right: 8px;
    border-radius: 2px;
    background-color: var(--el-color-primary);
  }

  .cat-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 8px;
  }

  .cat-item {
    background-color: #fff;
    border-radius: var(--app-card-radius);
    padding: 12px 4px;
    text-align: center;
  }

  .cat-name {
    font-size: 13px;
    font-weight: 600;
    color: #303133;
  }

  .cat-count {
    margin-top: 4px;
    font-size: 11px;
    color: #999;
  }

  .product-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 8px;
  }
</style>
