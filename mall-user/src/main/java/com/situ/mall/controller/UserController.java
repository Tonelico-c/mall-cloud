package com.situ.mall.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.situ.mall.annotation.MyLog;
import com.situ.mall.pojo.dto.LoginInfoDTO;
import com.situ.mall.pojo.entity.User;
import com.situ.mall.service.IUserService;
import com.situ.mall.utils.JwtUtil;
import com.situ.mall.utils.LoginContext;
import com.situ.mall.utils.PasswordUtil;
import com.situ.mall.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Gao
 * @since 2026-09-15
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private IUserService userService;

    @MyLog(module = "用户: 登录")
    @PostMapping("/login")
    public Result<String> login(@RequestBody LoginInfoDTO loginInfoDTO){


        User dbUser = userService.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getName, loginInfoDTO.getName()));
        if(dbUser == null){
            return Result.error("用户名不存在");
        }
        if(!PasswordUtil.matches(loginInfoDTO.getPassword(), dbUser.getPassword())){
            return Result.error("密码错误");
        }
        if(dbUser.getStatus() == 0){
            return Result.error("账号已禁用");
        }
        Map<String, Object> map = new HashMap<>();
        map.put("id", dbUser.getId());
        map.put("name", dbUser.getName());
        String token = JwtUtil.createToken(map);
        return Result.ok("登录成功",token);
    }

    @GetMapping("/userInfo")
    public Result<User> userInfo(){
        Map<String, Object> map = LoginContext.getLoginInfo();
        if(map == null){
            return Result.error("用户未登录");
        }
        Long id = (Long) map.get("id");
        User user = userService.getById(id);
        user.setPassword(null);
        return Result.ok(user);
    }

    @PutMapping
    public Result update(@RequestBody User user){
        Long id = (Long) LoginContext.getLoginInfo().get("id");
        user.setId(id);
        userService.updateById(user);
        return Result.ok("修改成功");
    }
}

