package com.situ.mall.service;

import com.situ.mall.pojo.entity.Order;
import com.baomidou.mybatisplus.extension.service.IService;
import com.situ.mall.pojo.vo.OrderVO;

import java.util.List;

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

    List<OrderVO> listItem();
}
