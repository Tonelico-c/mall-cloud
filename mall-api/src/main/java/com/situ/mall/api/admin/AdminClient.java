package com.situ.mall.api.admin;

import com.situ.mall.common.utils.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Set;

@FeignClient("admin-service")
public interface AdminClient {
    @GetMapping("/admins/selectAllImage")
    Result<Set<String>> selectAllImage();
}
