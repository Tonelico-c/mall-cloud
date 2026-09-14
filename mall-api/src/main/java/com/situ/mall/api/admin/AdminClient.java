package com.situ.mall.api.admin;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Set;

@FeignClient("admin-service")
public interface AdminClient {
    @GetMapping("/admin/selectAllImage")
    Set<String> selectAllImage();
}
