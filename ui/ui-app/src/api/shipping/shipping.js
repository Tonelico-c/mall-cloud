import request from "@/utils/request.js";

const shippingApi = {
    //当前用户的收货地址列表
    list() {
        return request.get("/shipping")
    },
    //新增收货地址
    add(shipping) {
        return request.post("/shipping", shipping)
    },
    //修改收货地址
    update(id, shipping) {
        return request.put(`/shipping/${id}`, shipping)
    },
    //设为默认地址
    setDefault(id) {
        return request.put(`/shipping/${id}/default`)
    },
    //删除收货地址
    deleteById(id) {
        return request.delete(`/shipping/${id}`)
    }
}

export default shippingApi
