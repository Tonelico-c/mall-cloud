import axios from 'axios'

const baseURL = '/api'
//这个request和axios具有相同的功能
const request = axios.create({ baseURL })

import { useTokenStore } from '@/store/token.js'
import { ElMessage } from "element-plus";
import router from '@/router'

//添加请求拦截器
request.interceptors.request.use(
    (config) => {
        //添加token
        const tokenStore = useTokenStore();
        //判断有没有token
        if (tokenStore.token) {
            config.headers.Authorization = tokenStore.token
        }
        //记录本次请求发出时使用的token，供响应拦截器识别"token过期前发出的旧请求"
        config._requestToken = tokenStore.token
        return config;
    },
    (error) => {
        //请求错误的回调
        Promise.reject(error)
    }
)


//添加响应的拦截器
request.interceptors.response.use(
    response => {
        //blob 响应(如 excel 导出)需要读取响应头里的文件名,返回完整 response
        if (response.config.responseType === 'blob') {
            return response;
        }
        //返回result
        return response.data
    },
    error => {
        //判断响应状态码,如果为401,则证明未登录,提示请登录,并跳转到登录页面
        if (error.response?.status === 401) {
            const tokenStore = useTokenStore()
            //请求发出时携带的token与当前token不一致，说明这是token过期后、重新登录之前发出的"迟到"响应
            //必须忽略：否则会清掉刚登录成功设置的新token，并再次把用户踢回登录页
            if (error.config?._requestToken !== tokenStore.token) {
                return Promise.reject(error)
            }
            //清除失效的token
            tokenStore.removeToken()
            //已经在登录页就只清理token,不再跳转
            //否则会把 /login?redirect=xxx 这个登录页自身的路径记成新的redirect,形成嵌套污染
            if (router.currentRoute.value.path === '/login') {
                return Promise.reject(error)
            }
            ElMessage.error('请先登录')
            //登录成功后跳回当前页面
            router.push({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } })
        } else {
            ElMessage.error('服务异常')
        }

        return Promise.reject(error);//异步的状态转化成失败的状态
    }
)

export default request
