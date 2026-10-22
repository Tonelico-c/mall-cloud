package com.situ.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.situ.mall.enums.OrderStatus;
import com.situ.mall.exception.ServiceException;
import com.situ.mall.mapper.OrderItemMapper;
import com.situ.mall.mapper.OrderMapper;
import com.situ.mall.pojo.entity.Order;
import com.situ.mall.pojo.entity.OrderItem;
import com.situ.mall.pojo.query.OrderQuery;
import com.situ.mall.pojo.vo.OrderVO;
import com.situ.mall.service.IOrderManageService;
import com.situ.mall.utils.LoginContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.List;

/**
 * <p>
 *  订单管理服务实现类
 * </p>
 *
 * @author Gao
 * @since 2026-10-20
 */
@Slf4j
@Service
public class OrderManageServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderManageService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;

    @Override
    public IPage<Order> list(OrderQuery orderQuery) {
        IPage<Order> page = new Page<>(orderQuery.getPage(), orderQuery.getLimit());
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(!ObjectUtils.isEmpty(orderQuery.getOrderNo()), Order::getOrderNo, orderQuery.getOrderNo())
                .eq(!ObjectUtils.isEmpty(orderQuery.getStatus()), Order::getStatus, orderQuery.getStatus())
                .between(!ObjectUtils.isEmpty(orderQuery.getBeginCreateTime()) && !ObjectUtils.isEmpty(orderQuery.getEndCreateTime()), Order::getCreateTime, orderQuery.getBeginCreateTime(), orderQuery.getEndCreateTime())
                .orderByDesc(Order::getCreateTime);
        return orderMapper.selectPage(page, queryWrapper);
    }

    /**
     * 订单详情：订单基本信息 + 该订单下的所有商品明细
     */
    @Override
    public OrderVO detail(Long orderNo) {
        Order order = orderMapper.selectById(orderNo);
        if (order == null) {
            throw new ServiceException("订单不存在");
        }
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order, orderVO);
        List<OrderItem> orderItemList = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderNo, orderNo));
        orderVO.setOrderItemList(orderItemList);
        return orderVO;
    }

    /**
     * 支付：只允许把当前用户的"未支付"订单改成"已支付",以影响行数作为幂等闸门
     * <p>
     * 支付成功后订单状态不再是"未支付",订单超时的延时消息在消费时会被直接丢弃,
     * 不会再把已支付的订单自动关单、回补库存
     */
    @Override
    public void pay(Long orderNo) {
        Long userId = (Long) LoginContext.getLoginInfo().get("id");
        UpdateWrapper<Order> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("order_no", orderNo)
                .eq("user_id", userId)
                .eq("status", OrderStatus.UNPAID.getCode())
                .set("status", OrderStatus.PAID.getCode())
                .set("payment_time", new Date());
        int rows = orderMapper.update(updateWrapper);
        if (rows == 0) {
            throw new ServiceException("只有未支付的订单才能支付");
        }
    }

    /**
     * 发货：只允许把"已支付"的订单改成"已发货",以影响行数作为幂等闸门
     */
    @Override
    public void deliver(Long orderNo) {
        UpdateWrapper<Order> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("order_no", orderNo)
                .eq("status", OrderStatus.PAID.getCode())
                .set("status", OrderStatus.DELIVERED.getCode())
                .set("send_time", new Date());
        int rows = orderMapper.update(updateWrapper);
        if (rows == 0) {
            // 订单不存在 / 不是已支付状态 / 已经被发过货，统一按状态不允许处理
            throw new ServiceException("只有已支付的订单才能发货");
        }
    }

    /**
     * 确认收货：只允许把当前用户的"已发货"订单改成"交易成功",同样以影响行数作为幂等闸门
     */
    @Override
    public void receive(Long orderNo) {
        Long userId = (Long) LoginContext.getLoginInfo().get("id");
        UpdateWrapper<Order> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("order_no", orderNo)
                .eq("user_id", userId)
                .eq("status", OrderStatus.DELIVERED.getCode())
                .set("status", OrderStatus.RECEIVED.getCode())
                .set("end_time", new Date());
        int rows = orderMapper.update(updateWrapper);
        if (rows == 0) {
            throw new ServiceException("只有已发货的订单才能确认收货");
        }
    }
}
