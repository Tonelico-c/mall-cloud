package com.situ.mall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.situ.mall.pojo.entity.Order;
import com.situ.mall.pojo.query.OrderQuery;
import com.situ.mall.pojo.vo.OrderVO;

/**
 * <p>
 * 订单管理服务类（后台查询 + 发货，app 端确认收货）
 * </p>
 *
 * @author Gao
 * @since 2026-10-20
 */
public interface IOrderManageService extends IService<Order> {

    IPage<Order> list(OrderQuery orderQuery);

    OrderVO detail(Long orderNo);

    void pay(Long orderNo);

    void deliver(Long orderNo);

    void receive(Long orderNo);
}
