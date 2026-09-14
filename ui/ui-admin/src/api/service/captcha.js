import request from '@/utils/request.js'

//将针对Admin的所有请求封装到一个对象里面
const captchaApi = {
    captcha() {
        return request.get('/service/captcha')
    }
}

export default captchaApi