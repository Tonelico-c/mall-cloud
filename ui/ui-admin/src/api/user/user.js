import request from "@/utils/request.js";

const userApi = {
    list(userQuery) {
        return request.get("/user", {params: userQuery});
    },
    deleteById(id) {
        return request.delete(`/user/${id}`);
    },
    add(user) {
        return request.post("/user", user)
    },
    selectById(id) {
        return request.get(`/user/${id}`)
    },
    update(id, user) {
        return request.put(`/user/${id}`, user)
    },
    deleteAll(ids) {
        // axios 的 delete 第2个参数是 config，请求体必须放在 data 字段里
        return request.delete("/user", {data: ids})
    }
}

export default userApi
