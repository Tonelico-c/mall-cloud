import request from "@/utils/request.js";

const orderApi = {
    //根据购物车中已勾选的商品生成订单 {shippingId}
    create(order) {
        return request.post("/order", order)
    },
    //当前用户的订单列表，每项包含订单商品orderItemList
    list() {
        return request.get("/order")
    }
}

export default orderApi
