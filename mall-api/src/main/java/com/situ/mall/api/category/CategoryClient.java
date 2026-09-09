package com.situ.mall.api.category;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "category-service")
public interface CategoryClient {
    @GetMapping("/category/name/{id}")
    String getCategoryNameById(@PathVariable Long id);
}
