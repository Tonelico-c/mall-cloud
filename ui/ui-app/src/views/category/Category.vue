<script setup>
  import {ref, computed, watch} from 'vue'
  import {useRoute, useRouter} from 'vue-router'
  import {ArrowRight} from '@element-plus/icons-vue'
  import categoryApi from '@/api/category/category.js'
  import productApi from '@/api/product/product.js'
  import ProductCard from '@/components/ProductCard.vue'

  const route = useRoute()
  const router = useRouter()

  //一级分类列表
  const categoryList = ref([])
  //当前选中一级分类的下标
  const activeIndex = ref(0)
  //当前选中二级分类的id
  const activeSubId = ref(null)

  categoryApi.tree().then(result => {
    if (result.code === 1) {
      categoryList.value = result.data || []
      //支持从首页跳转过来时携带要定位的一级分类id
      locateCategory()
      //默认选中当前一级分类下面的第一个二级分类
      activeSubId.value = (currentCategory.value?.children || [])[0]?.id ?? null
    }
  })

  //根据路由参数定位到指定的一级分类
  const locateCategory = () => {
    const id = route.query.id
    if (id) {
      const index = categoryList.value.findIndex(category => String(category.id) === String(id))
      if (index >= 0) {
        activeIndex.value = index
      }
    }
  }

  //首页切换分类时路由query变化，重新定位
  watch(() => route.query.id, () => {
    if (categoryList.value.length > 0) {
      locateCategory()
    }
  })

  //当前选中的一级分类
  const currentCategory = computed(() => categoryList.value[activeIndex.value])

  //当前选中的二级分类
  const activeSub = computed(() =>
    (currentCategory.value?.children || []).find(sub => sub.id === activeSubId.value))

  //切换一级分类时，默认选中它下面的第一个二级分类
  watch(activeIndex, () => {
    activeSubId.value = (currentCategory.value?.children || [])[0]?.id ?? null
  })

  //当前二级分类下的前10个商品
  const productList = ref([])
  const productLoading = ref(false)

  const loadProducts = (sub) => {
    productList.value = []
    productLoading.value = true
    productApi.list({categoryId: sub.id, page: 1, limit: 10}).then(result => {
      if (result.code === 1) {
        //只展示在售商品
        productList.value = (result.data.records || []).filter(item => item.status === 1)
      }
      productLoading.value = false
    }).catch(() => {
      productLoading.value = false
    })
  }

  //选中的二级分类变化时加载它的商品
  watch(activeSub, (sub) => {
    if (sub) {
      loadProducts(sub)
    } else {
      productList.value = []
    }
  })

  //更多商品：跳转商品列表页
  const toProductList = (sub) => {
    router.push({path: '/product', query: {categoryId: sub.id, title: sub.name}})
  }
</script>

<template>
  <div class="cat-page">
    <!-- 页面头部 -->
    <header class="cat-header">分类</header>

    <div class="cat-body">
      <!-- 左侧一级分类 -->
      <aside class="cat-left">
        <div v-for="(category, index) in categoryList" :key="category.id"
             class="cat-left-item" :class="{active: index === activeIndex}"
             @click="activeIndex = index">
          {{ category.name }}
        </div>
      </aside>

      <!-- 右侧二级分类和商品 -->
      <section class="cat-right">
        <template v-if="currentCategory">
          <div class="cat-right-title">{{ currentCategory.name }}</div>
          <div v-if="(currentCategory.children || []).length > 0" class="sub-grid">
            <div v-for="sub in currentCategory.children" :key="sub.id"
                 class="sub-item" :class="{active: sub.id === activeSubId}"
                 @click="activeSubId = sub.id">
              <div class="sub-name ellipsis-1">{{ sub.name }}</div>
            </div>
          </div>
          <el-empty v-else description="该分类下暂无子分类"/>

          <!-- 选中二级分类下的前10个商品 -->
          <template v-if="activeSub">
            <div class="sub-product-title">{{ activeSub.name }}</div>
            <div v-if="productList.length > 0" class="product-grid">
              <ProductCard v-for="product in productList" :key="product.id" :product="product"/>
            </div>
            <el-empty v-else-if="!productLoading" description="该分类下暂无商品" :image-size="60"/>
            <div v-else class="product-loading">商品加载中...</div>

            <!-- 更多商品：跳转商品列表页，当前分类下没有商品时不展示 -->
            <div v-if="productList.length > 0" class="more-bar" @click="toProductList(activeSub)">
              <span>更多商品</span>
              <el-icon><ArrowRight/></el-icon>
            </div>
          </template>
        </template>
        <el-empty v-else description="分类加载中..."/>
      </section>
    </div>
  </div>
</template>

<style scoped>
  .cat-page {
    display: flex;
    flex-direction: column;
    /*动态视口高度，真机浏览器地址栏弹出时也不会超出可视区域*/
    height: 100dvh;
  }

  .cat-header {
    flex-shrink: 0;
    height: 44px;
    line-height: 44px;
    text-align: center;
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    background-color: #fff;
    border-bottom: 1px solid #f0f0f0;
  }

  .cat-body {
    min-height: 0;
    display: flex;
    /*减去头部和底部TabBar的高度，左右两侧各自滚动；不能用flex:1，flex-basis:0%会让这里的height失效*/
    height: calc(100dvh - 94px - env(safe-area-inset-bottom));
  }

  .cat-left {
    width: 96px;
    flex-shrink: 0;
    overflow-y: auto;
    background-color: #fff;
  }

  .cat-left-item {
    padding: 14px 8px;
    text-align: center;
    font-size: 13px;
    color: #666;
    position: relative;
  }

  .cat-left-item.active {
    background-color: var(--app-bg);
    color: var(--el-color-primary);
    font-weight: 600;
  }

  /*选中项左侧的红色指示条*/
  .cat-left-item.active::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 3px;
    height: 16px;
    border-radius: 2px;
    background-color: var(--el-color-primary);
  }

  .cat-right {
    flex: 1;
    overflow-y: auto;
    padding: 8px;
  }

  .cat-right-title {
    padding: 6px 4px 10px;
    font-size: 14px;
    font-weight: 600;
    color: #303133;
  }

  .sub-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 8px;
  }

  .sub-item {
    background-color: #fff;
    border-radius: var(--app-card-radius);
    /*透明占位边框，选中时换成主题色，避免尺寸抖动*/
    border: 1px solid transparent;
    padding: 17px 4px;
    text-align: center;
  }

  .sub-item.active {
    border-color: var(--el-color-primary);
    color: var(--el-color-primary);
    background-color: var(--el-color-primary-light-9);
  }

  .sub-item.active .sub-name {
    color: var(--el-color-primary);
    font-weight: 600;
  }

  .sub-name {
    font-size: 13px;
    color: #303133;
  }

  .sub-product-title {
    padding: 14px 4px 10px;
    font-size: 14px;
    font-weight: 600;
    color: #303133;
  }

  .product-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 8px;
  }

  .product-loading {
    padding: 30px 0;
    text-align: center;
    font-size: 13px;
    color: #999;
  }

  .more-bar {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 4px;
    margin-top: 10px;
    padding: 11px 0;
    background-color: #fff;
    border-radius: var(--app-card-radius);
    font-size: 13px;
    color: #666;
  }
</style>
