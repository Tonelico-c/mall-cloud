package com.situ.mall.controller;


import com.situ.mall.pojo.entity.Order;
import com.situ.mall.pojo.vo.OrderVO;
import com.situ.mall.service.IOrderService;
import com.situ.mall.utils.Result;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Gao
 * @since 2026-09-17
 */
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private IOrderService orderService;

    @PostMapping
    public Result add(@RequestBody Order order){
        orderService.add(order);
        return Result.ok("添加成功");
    }

    @GetMapping
    public Result<List<OrderVO>> list(){
        List<OrderVO> orderList = orderService.listItem();
        return Result.ok(orderList);
    }
}

