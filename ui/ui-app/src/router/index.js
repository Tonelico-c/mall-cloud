// 创建一个路由器，并暴露出去
// 第一步：引入createRouter
import {createRouter, createWebHistory} from 'vue-router'
// 引入一个一个可能要呈现组件
import Login from '@/views/Login.vue'
import Layout from '@/views/Layout.vue'
import Home from '@/views/Home.vue'
import Category from '@/views/category/Category.vue'
import ProductList from '@/views/product/ProductList.vue'
import ProductDetail from '@/views/product/ProductDetail.vue'
import Cart from '@/views/cart/Cart.vue'
import OrderConfirm from '@/views/order/OrderConfirm.vue'
import OrderList from '@/views/order/OrderList.vue'
import User from '@/views/user/User.vue'
import Profile from '@/views/user/Profile.vue'
import Shipping from '@/views/shipping/Shipping.vue'

//创建路由器
const router = createRouter({
    history: createWebHistory(),
    routes: [
        {path: '/login', component: Login},
        {
            path: '/', component: Layout, children: [
                {path: '', redirect: '/home'},
                //底部TabBar显示的四个页面，使用meta.tabbar标记
                {path: '/home', component: Home, meta: {tabbar: true}},
                {path: '/category', component: Category, meta: {tabbar: true}},
                {path: '/cart', component: Cart, meta: {tabbar: true}},
                {path: '/user', component: User, meta: {tabbar: true}},
                //其余子页面隐藏底部TabBar
                {path: '/product', component: ProductList},
                {path: '/product/:id', component: ProductDetail},
                {path: '/order/confirm', component: OrderConfirm},
                {path: '/order', component: OrderList},
                {path: '/user/profile', component: Profile},
                {path: '/shipping', component: Shipping}
            ]
        }
    ]
})

//路由守卫
//全局前置守卫
import {useTokenStore} from '@/store/token.js'
let whiteList = ['/login']; // 白名单
router.beforeEach((to, from, next) => {
    const tokenStore = useTokenStore()
    const token = tokenStore.token;
    //网关对所有接口（除/user/login等白名单）都要求携带token，所以浏览也必须先登录
    if (!whiteList.includes(to.path) && !token) {
        //登录成功后跳回原本想访问的页面
        next('/login?redirect=' + encodeURIComponent(to.fullPath))
    } else {
        next()
    }
})

// 暴露出去router
export default router
