import request from "@/utils/request.js";

const cartApi = {
    //当前用户的购物车列表，每项包含商品信息product
    list() {
        return request.get("/cart")
    },
    //加入购物车 {productId, count}
    add(cart) {
        return request.post("/cart", cart)
    },
    //修改数量/选中状态 {id, count, selected}
    update(cart) {
        return request.put("/cart", cart)
    },
    //删除购物车项
    deleteById(id) {
        return request.delete(`/cart/${id}`)
    }
}

export default cartApi
