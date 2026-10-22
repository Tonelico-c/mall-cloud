package com.situ.mall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.situ.mall.pojo.entity.User;
import com.situ.mall.pojo.query.UserQuery;

/**
 * <p>
 *  用户管理（后台）服务类
 * </p>
 *
 * @author Gao
 * @since 2026-10-20
 */
public interface IUserManageService extends IService<User> {

    IPage<User> list(UserQuery userQuery);

    void add(User user);
}
