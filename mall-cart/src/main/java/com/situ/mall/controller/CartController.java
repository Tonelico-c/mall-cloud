package com.situ.mall.controller;


import com.situ.mall.pojo.entity.Cart;
import com.situ.mall.pojo.vo.CartVO;
import com.situ.mall.service.ICartService;
import com.situ.mall.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping("/cart")
public class CartController {
    @Autowired
    private ICartService cartService;

    @PostMapping
    public Result add(@RequestBody Cart cart){
        cartService.add(cart);
        return Result.ok("添加成功");
    }

    @GetMapping
    public Result<List<CartVO>> list(){
        List<CartVO> cartList = cartService.listVO();
        return Result.ok(cartList);
    }

    @PutMapping
    public Result update(@RequestBody Cart cart){
        cartService.update(cart);
        return Result.ok("更新成功");
    }
}

