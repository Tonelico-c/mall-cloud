// 创建一个路由器，并暴露出去
// 第一步：引入createRouter
import {createRouter, createWebHistory} from 'vue-router'
// 引入一个一个可能要呈现组件
import Index from '@/views/Index.vue'
import Login from '@/views/Login.vue'
import Admin from '@/views/admin/Admin.vue'
import Product from '@/views/product/Product.vue'
import Category from '@/views/category/Category.vue';


//创建路由器
const router = createRouter({
    history: createWebHistory(),
    routes: [
        {path: '/login', component: Login},
        {
            path: '/', component: Index, children: [
                {path: '/admin', component: Admin},
                {path: '/product', component: Product},
                {path: '/category', component: Category},
                {path: '/admin/info', component: AdminInfo},
            ]
        }
    ]
})

//路由守卫
//全局前置守卫
import {useTokenStore} from '@/store/token.js'
import AdminInfo from "@/views/admin/AdminInfo.vue";
let whiteList = ['/login']; // 白名单
router.beforeEach((to, from, next) => {
    const tokenStore = useTokenStore()
    const token = tokenStore.token;
    if (!whiteList.includes(to.path) && !token) {
        next('/login')
    } else {
        next()
    }
})

// 暴露出去router
export default router