package com.situ.mall.pojo.dto;

import lombok.Data;

@Data
public class LoginInfoDTO {
    private String name;
    private String password;
    private String captcha;
    private String uuid;
}
