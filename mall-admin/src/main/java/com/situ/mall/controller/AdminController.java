package com.situ.mall.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.mall.common.utils.JwtUtil;
import com.situ.mall.common.utils.PasswordUtil;
import com.situ.mall.common.utils.Result;
import com.situ.mall.pojo.dto.AdminPasswordDTO;
import com.situ.mall.pojo.entity.Admin;
import com.situ.mall.pojo.query.AdminQuery;
import com.situ.mall.service.IAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Gao
 * @since 2026-09-07
 */
@RestController
@RequestMapping("/admins")
public class AdminController {

    @Autowired
    private IAdminService adminService;


    @PostMapping("/login")
    public Result<String> login(@RequestBody Admin admin){
        Admin dbAdmin = adminService.getOne(new LambdaQueryWrapper<Admin>()
                        .eq(Admin::getName, admin.getName()));
        if(dbAdmin == null){
            return Result.error("用户名不存在");
        }
        if(!PasswordUtil.matches(admin.getPassword(), dbAdmin.getPassword())){
            return Result.error("密码错误");
        }
        /*if(!admin.getPassword().equals(dbAdmin.getPassword())){
            return Result.error("密码错误");
        }*/
        if(dbAdmin.getStatus() == 0){
            return Result.error("账号已禁用");
        }
        Map<String, Object> map = new HashMap<>();
        map.put("id", dbAdmin.getId());
        map.put("name", dbAdmin.getName());
        String token = JwtUtil.createToken(map);
        return Result.ok("登录成功",token);
    }
    // 修改密码
    @PutMapping("/resetPassword")
    public Result resetPassword(@RequestHeader("Authorization") String token, @RequestBody AdminPasswordDTO adminPasswordDTO){
        Map<String, Object> map = JwtUtil.parseToken(token);
        Integer id = (Integer) map.get("id");
        Admin user = adminService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        if (!user.getPassword().equals(adminPasswordDTO.getOldPassword())) {
            return Result.error("原密码错误");
        }
        Admin newUser =  new Admin();
        newUser.setId(user.getId());
        newUser.setPassword(PasswordUtil.hash(adminPasswordDTO.getNewPassword()));
        adminService.updateById(newUser);
        return Result.ok("密码修改成功");
    }
    // 获取当前登录管理员信息
    @GetMapping("/adminInfo")
    public Result<Admin> adminInfo(@RequestHeader("Authorization") String token){
        Map<String, Object> map = JwtUtil.parseToken(token);
        Integer id = (Integer) map.get("id");
        Admin admin = adminService.getById(id);
        admin.setPassword(null);
        Map<String, Object> adminMap = new HashMap<>();
        adminMap.put("admin", admin);
        return Result.ok(adminMap);
    }

    @GetMapping
    public Result<IPage<Admin>> list(AdminQuery adminQuery){
        IPage<Admin> page = adminService.list(adminQuery);
        return Result.ok(page);
    }

    @GetMapping("/{id}")
    public Result<Admin> selectById(@PathVariable Long id){
        return Result.ok(adminService.getById(id));
    }

    @GetMapping("/selectAllImage")
    Set<String> selectAllImage() {
        return adminService.selectAllImage();
    }

    @PostMapping
    public Result add(@RequestBody Admin admin){
        adminService.add(admin);
        return Result.ok("添加成功");
    }
    @PutMapping("/{id}")
    public Result update(@PathVariable Long id,@RequestBody Admin admin){
        admin.setId(id);
        adminService.updateById(admin);
        return Result.ok("修改成功");
    }
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id){
        adminService.removeById(id);
        return Result.ok("删除成功");
    }
    @DeleteMapping
    public Result deleteBatch(@RequestBody Long[] ids) {
        adminService.removeByIds(java.util.Arrays.asList(ids));
        return Result.ok("批量删除成功");
    }
}

