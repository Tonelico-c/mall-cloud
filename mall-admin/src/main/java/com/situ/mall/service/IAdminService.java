package com.situ.mall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.mall.pojo.entity.Admin;
import com.baomidou.mybatisplus.extension.service.IService;
import com.situ.mall.pojo.query.AdminQuery;

import java.util.Set;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Gao
 * @since 2026-09-07
 */
public interface IAdminService extends IService<Admin> {

    IPage<Admin> list(AdminQuery adminQuery);

    void add(Admin admin);

    Set<String> selectAllImage();
}
