import request from "@/utils/request.js";

const adminApi = {
    list(adminQuery) {
        return request.get("/admins", {params: adminQuery});
    },
    deleteById(id) {
        //return service.delete("/admins/" + id);
        return request.delete(`/admins/${id}`);
    },
    add(admin) {
        return request.post("/admins", admin)
    },
    selectById(id) {
        return request.get(`/admins/${id}`)
    },
    update(id, admin) {
        return request.put(`/admins/${id}`, admin)
    },
    deleteAll(ids) {
        // axios 的 delete 第2个参数是 config，请求体必须放在 data 字段里
        return request.delete("/admins", {data: ids})
    },
    login(admin) {
        return request.post("/admins/login", admin)
    },
    adminInfo() {
        return request.get("/admins/adminInfo")
    },
    resetPassword(adminPasswordDTO) {
        return request.put("/admins/resetPassword", adminPasswordDTO)
    }
}

export default  adminApi