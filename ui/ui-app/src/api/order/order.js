import request from "@/utils/request.js";

const orderApi = {
    //根据购物车中已勾选的商品生成订单 {shippingId}
    create(order) {
        return request.post("/order", order)
    },
    //当前用户的订单列表，每项包含订单商品orderItemList
    list() {
        return request.get("/order")
    },
    //支付：订单状态由"未支付"变为"已支付"
    pay(orderNo) {
        return request.put(`/order/${orderNo}/pay`)
    },
    //确认收货：订单状态由"已发货"变为"交易成功"
    receive(orderNo) {
        return request.put(`/order/${orderNo}/receive`)
    }
}

export default orderApi
