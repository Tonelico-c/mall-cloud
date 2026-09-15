import request from "@/utils/request.js";

const categoryApi = {
    //分类树形结构，首页分类宫格和分类页都使用
    tree() {
        return request.get("/category/tree")
    }
}

export default categoryApi
