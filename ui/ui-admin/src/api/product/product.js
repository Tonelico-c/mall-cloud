import request from "@/utils/request.js";

const productApi = {
    list(productQuery) {
        return request.get("/product", {params: productQuery});
    },
    deleteById(id) {
        return request.delete(`/product/${id}`);
    },
    add(product) {
        return request.post("/product", product)
    },
    selectById(id) {
        return request.get(`/product/${id}`)
    },
    update(id, product) {
        return request.put(`/product/${id}`, product)
    },
    // axios 的 delete 第2个参数是 config，请求体必须放在 data 字段里
    deleteAll(ids) {
        return request.delete("/product", {data: ids})
    }
}

export default productApi
