package com.situ.mall.controller;


import com.situ.mall.pojo.entity.Shipping;
import com.situ.mall.service.IShippingService;
import com.situ.mall.utils.Result;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Gao
 * @since 2026-09-16
 */
@RestController
@RequestMapping("/shipping")
public class ShippingController {
    @Autowired
    private IShippingService shippingService;

    @GetMapping
    public Result<List<Shipping>> list(){
        List<Shipping> shippingList = shippingService.listUserShapping();
        return Result.ok(shippingList);
    }
}

