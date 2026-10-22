import request from "@/utils/request.js";

const orderApi = {
    //订单分页查询 {orderNo, status, beginCreateTime, endCreateTime, page, limit}
    list(orderQuery) {
        return request.get("/order/page", {params: orderQuery});
    },
    //订单详情：订单基本信息 + 该订单的商品明细
    detail(orderNo) {
        return request.get(`/order/${orderNo}/items`)
    },
    //发货：订单状态由"已支付"变为"已发货"
    deliver(orderNo) {
        return request.put(`/order/${orderNo}/deliver`)
    }
}

export default orderApi
