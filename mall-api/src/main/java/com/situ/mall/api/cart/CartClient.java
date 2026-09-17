package com.situ.mall.api.cart;

import com.situ.mall.api.pojo.vo.CartVO;
import com.situ.mall.utils.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(value = "cart-service")
public interface CartClient {
    @GetMapping("/cart")
    public Result<List<CartVO>> list();
}
