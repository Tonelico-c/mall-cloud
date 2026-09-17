package com.situ.mall.controller;


import com.situ.mall.pojo.entity.Order;
import com.situ.mall.service.IOrderService;
import com.situ.mall.utils.Result;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

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

}

