package com.situ.mall.pojo.dto;

import lombok.Data;

@Data
public class AdminLoginDTO {
    private String name;
    private String password;
    private String captcha;
}
