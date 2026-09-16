package com.situ.mall.controller;


import com.situ.mall.pojo.entity.Cart;
import com.situ.mall.service.ICartService;
import com.situ.mall.utils.Result;
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
 * @since 2026-09-16
 */
@RestController
@RequestMapping("/cart")
public class CartController {
    @Autowired
    private ICartService cartService;

    @PostMapping
    public Result add(@RequestBody Cart cart){
        cartService.add(cart);
        return Result.ok("添加成功");
    }


}

