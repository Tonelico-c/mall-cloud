package com.situ.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.situ.mall.pojo.entity.Shipping;
import com.situ.mall.mapper.ShippingMapper;
import com.situ.mall.service.IShippingService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.situ.mall.utils.LoginContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Gao
 * @since 2026-09-16
 */
@Service
public class ShippingServiceImpl extends ServiceImpl<ShippingMapper, Shipping> implements IShippingService {

    @Autowired
    private ShippingMapper shippingMapper;

    @Override
    public List<Shipping> listUserShapping() {
        Long id = (Long) LoginContext.getLoginInfo().get("id");
        LambdaQueryWrapper<Shipping> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Shipping::getUserId, id);
        return shippingMapper.selectList(queryWrapper);
    }
}
