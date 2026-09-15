import {defineStore} from 'pinia'
import cartApi from '@/api/cart/cart.js'

export const useCartCountStore = defineStore('cartCount', {
    //存储数据地方
    state() {
        return {
            //购物车中所有商品的数量之和，用于底部TabBar的角标
            count: 0
        }
    },
    //actions里面放的是一个一个方法
    actions: {
        //从后台重新查询购物车，计算总数量
        refreshCount() {
            return cartApi.list().then(result => {
                if (result.code === 1) {
                    this.count = (result.data || []).reduce((sum, item) => sum + (item.count || 0), 0)
                }
            }).catch(() => {
                //静默失败，不打扰用户（购物车角标不是关键功能）
            })
        },
        //已知购物车列表时直接设置总数量，避免重复请求
        setCount(count) {
            this.count = count
        },
        clear() {
            this.count = 0
        }
    }
})
