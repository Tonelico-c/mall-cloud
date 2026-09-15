import request from "@/utils/request.js";

const productApi = {
    //分页查询商品列表，GET /product?page=1&limit=10&name=xxx&categoryId=xxx
    list(productQuery) {
        return request.get("/product", {params: productQuery})
    },
    //根据ID查询商品详情
    selectById(id) {
        return request.get(`/product/${id}`)
    }
}

export default productApi
