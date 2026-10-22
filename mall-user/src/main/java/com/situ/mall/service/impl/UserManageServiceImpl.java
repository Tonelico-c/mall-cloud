package com.situ.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.situ.mall.exception.ServiceException;
import com.situ.mall.mapper.UserMapper;
import com.situ.mall.pojo.entity.User;
import com.situ.mall.pojo.query.UserQuery;
import com.situ.mall.service.IUserManageService;
import com.situ.mall.utils.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * <p>
 *  用户管理（后台）服务实现类
 * </p>
 *
 * @author Gao
 * @since 2026-10-20
 */
@Service
public class UserManageServiceImpl extends ServiceImpl<UserMapper, User> implements IUserManageService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public IPage<User> list(UserQuery userQuery) {
        IPage<User> page = new Page<>(userQuery.getPage(), userQuery.getLimit());
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(!ObjectUtils.isEmpty(userQuery.getName()), User::getName, userQuery.getName())
                .like(!ObjectUtils.isEmpty(userQuery.getEmail()), User::getEmail, userQuery.getEmail())
                .between(!ObjectUtils.isEmpty(userQuery.getBeginCreateTime()) && !ObjectUtils.isEmpty(userQuery.getEndCreateTime()), User::getCreateTime, userQuery.getBeginCreateTime(), userQuery.getEndCreateTime());
        return userMapper.selectPage(page, queryWrapper);
    }

    @Override
    public void add(User user) {
        // 按用户名查库，做唯一性校验
        User dbUser = userMapper.selectOne(new QueryWrapper<User>().eq("name", user.getName()));
        if (dbUser != null) {
            log.error("添加失败，用户名已存在");
            throw new ServiceException("用户名已存在");
        }
        user.setPassword(PasswordUtil.hash(user.getPassword()));
        userMapper.insert(user);
    }
}
