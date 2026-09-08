import request from "@/utils/request.js";

const productApi = {
    list(productQuery) {
        return request.get("/products", {params: productQuery});
    },
    deleteById(id) {
        return request.delete(`/products/${id}`);
    },
    add(product) {
        return request.post("/products", product)
    },
    selectById(id) {
        return request.get(`/products/${id}`)
    },
    update(id, product) {
        return request.put(`/products/${id}`, product)
    },
    // axios 的 delete 第2个参数是 config，请求体必须放在 data 字段里
    deleteAll(ids) {
        return request.delete("/products", {data: ids})
    }
}

export default productApi
