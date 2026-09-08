package com.situ.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.situ.mall.common.exception.ServiceException;
import com.situ.mall.common.utils.PasswordUtil;
import com.situ.mall.pojo.entity.Admin;
import com.situ.mall.mapper.AdminMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.situ.mall.pojo.query.AdminQuery;
import com.situ.mall.service.IAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Gao
 * @since 2026-09-07
 */
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements IAdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Override
    public IPage<Admin> list(AdminQuery adminQuery) {
        IPage<Admin> page = new Page<>(adminQuery.getPage(), adminQuery.getLimit());
        LambdaQueryWrapper<Admin> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(!ObjectUtils.isEmpty(adminQuery.getName()), Admin::getName, adminQuery.getName())
                    .like(!ObjectUtils.isEmpty(adminQuery.getEmail()), Admin::getEmail, adminQuery.getEmail())
                .between(!ObjectUtils.isEmpty(adminQuery.getBeginCreateTime()) && !ObjectUtils.isEmpty(adminQuery.getEndCreateTime()), Admin::getCreateTime, adminQuery.getBeginCreateTime(), adminQuery.getEndCreateTime());
        return adminMapper.selectPage(page, queryWrapper);
    }

    @Override
    public void add(Admin admin) {
        // 按用户名查库，做唯一性校验
        Admin dbUser = adminMapper.selectOne(new QueryWrapper<Admin>().eq("name", admin.getName()));
        if(dbUser != null){
            log.error("添加失败，用户名已存在");
            throw new ServiceException("用户名已存在");
        }
        admin.setPassword(PasswordUtil.hash(admin.getPassword()));
        adminMapper.insert(admin);
    }
}
