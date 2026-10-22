package com.situ.mall.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.mall.pojo.entity.User;
import com.situ.mall.pojo.query.UserQuery;
import com.situ.mall.service.IUserManageService;
import com.situ.mall.utils.PasswordUtil;
import com.situ.mall.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  用户管理（后台）前端控制器：用户的基本增删改查
 * </p>
 *
 * @author Gao
 * @since 2026-10-20
 */
@RestController
@RequestMapping("/user")
public class UserManageController {

    @Autowired
    private IUserManageService userManageService;

    @GetMapping
    public Result<IPage<User>> list(UserQuery userQuery) {
        IPage<User> page = userManageService.list(userQuery);
        // 列表不下发密码
        page.getRecords().forEach(user -> user.setPassword(null));
        return Result.ok(page);
    }

    @GetMapping("/{id}")
    public Result<User> selectById(@PathVariable Long id) {
        User user = userManageService.getById(id);
        if (user != null) {
            // 编辑回显不带密码，密码是否修改由编辑页单独决定
            user.setPassword(null);
        }
        return Result.ok(user);
    }

    @PostMapping
    public Result add(@RequestBody User user) {
        userManageService.add(user);
        return Result.ok("添加成功");
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        // 编辑时没填新密码就保持原密码不变
        if (StringUtils.hasText(user.getPassword())) {
            user.setPassword(PasswordUtil.hash(user.getPassword()));
        } else {
            user.setPassword(null);
        }
        userManageService.updateById(user);
        return Result.ok("修改成功");
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        userManageService.removeById(id);
        return Result.ok("删除成功");
    }

    @DeleteMapping
    public Result deleteBatch(@RequestBody Long[] ids) {
        userManageService.removeByIds(java.util.Arrays.asList(ids));
        return Result.ok("批量删除成功");
    }
}
