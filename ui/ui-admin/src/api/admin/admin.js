import request from "@/utils/request.js";

const adminApi = {
    list(adminQuery) {
        return request.get("/admin", {params: adminQuery});
    },
    deleteById(id) {
        //return service.delete("/admins/" + id);
        return request.delete(`/admin/${id}`);
    },
    add(admin) {
        return request.post("/admin", admin)
    },
    selectById(id) {
        return request.get(`/admin/${id}`)
    },
    update(id, admin) {
        return request.put(`/admin/${id}`, admin)
    },
    deleteAll(ids) {
        // axios 的 delete 第2个参数是 config，请求体必须放在 data 字段里
        return request.delete("/admin", {data: ids})
    },
    login(admin) {
        return request.post("/admin/login", admin)
    },
    adminInfo() {
        return request.get("/admin/adminInfo")
    },
    resetPassword(adminPasswordDTO) {
        return request.put("/admin/resetPassword", adminPasswordDTO)
    }
}

export default  adminApi