package com.situ.mall.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.mall.pojo.entity.Order;
import com.situ.mall.pojo.query.OrderQuery;
import com.situ.mall.pojo.vo.OrderVO;
import com.situ.mall.service.IOrderManageService;
import com.situ.mall.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 订单管理前端控制器：后台订单查询、详情、发货，app 端确认收货
 * </p>
 *
 * @author Gao
 * @since 2026-10-20
 */
@RestController
@RequestMapping("/order")
public class OrderManageController {

    @Autowired
    private IOrderManageService orderManageService;

    // 后台订单列表（只做查询）
    @GetMapping("/page")
    public Result<IPage<Order>> list(OrderQuery orderQuery) {
        IPage<Order> page = orderManageService.list(orderQuery);
        return Result.ok(page);
    }

    // 订单详情：订单信息 + 该订单的商品明细
    @GetMapping("/{orderNo}/items")
    public Result<OrderVO> detail(@PathVariable Long orderNo) {
        return Result.ok(orderManageService.detail(orderNo));
    }

    // 支付：订单状态由"未支付"变为"已支付"
    @PutMapping("/{orderNo}/pay")
    public Result pay(@PathVariable Long orderNo) {
        orderManageService.pay(orderNo);
        return Result.ok("支付成功");
    }

    // 发货：订单状态由"已支付"变为"已发货"
    @PutMapping("/{orderNo}/deliver")
    public Result deliver(@PathVariable Long orderNo) {
        orderManageService.deliver(orderNo);
        return Result.ok("发货成功");
    }

    // 确认收货：订单状态由"已发货"变为"交易成功"
    @PutMapping("/{orderNo}/receive")
    public Result receive(@PathVariable Long orderNo) {
        orderManageService.receive(orderNo);
        return Result.ok("确认收货成功");
    }
}
