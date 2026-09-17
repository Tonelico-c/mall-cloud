package com.situ.mall.service;

import com.situ.mall.pojo.entity.Order;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Gao
 * @since 2026-09-17
 */
public interface IOrderService extends IService<Order> {

    void add(Order order);
}
