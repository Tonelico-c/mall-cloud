import {defineStore} from 'pinia'

export const useAdminInfoStore = defineStore('adminInfo', {
    //存储数据地方
    state() {
        return {
            admin: ''
        }
    },
    //actions里面放的是一个一个方法
    actions: {
        setAdminInfo(admin) {
            console.log(admin)
            this.admin = admin
        },
        removeAdminInfo(value) {
            this.admin = ''
        }
    },
    persist: {
        enabled: true, // 开启缓存  默认会存储在本地localstorage
    }
})