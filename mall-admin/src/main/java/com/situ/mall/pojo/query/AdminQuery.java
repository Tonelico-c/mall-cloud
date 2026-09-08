package com.situ.mall.pojo.query;

import lombok.Data;

import java.util.Date;

@Data
public class AdminQuery {
    private String name;
    private String email;
    private Date beginCreateTime;
    private Date endCreateTime;
    private Integer page;
    private Integer limit;
}
