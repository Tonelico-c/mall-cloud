import request from "@/utils/request.js";

const categoryApi = {
    //分类树形结构，分类管理和商品的分类下拉都使用
    tree() {
        return request.get("/category/tree")
    },
    deleteById(id) {
        return request.delete(`/category/${id}`)
    },
    add(category) {
        return request.post("/category", category)
    },
    selectById(id) {
        return request.get(`/category/${id}`)
    },
    update(id, category) {
        return request.put(`/category/${id}`, category)
    }
}

export default categoryApi