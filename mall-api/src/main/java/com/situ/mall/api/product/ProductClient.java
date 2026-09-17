package com.situ.mall.api.product;

import com.situ.mall.api.pojo.entity.Product;
import com.situ.mall.utils.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Set;

@FeignClient("product-service")
public interface ProductClient {
    @GetMapping("/product/selectAllImage")
    Set<String> selectAllImage();

    @GetMapping("/product/{id}")
    Result<Product> selectById(@PathVariable Long id);
}
