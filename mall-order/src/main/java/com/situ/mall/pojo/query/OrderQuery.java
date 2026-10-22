package com.situ.mall.pojo.query;

import lombok.Data;

import java.util.Date;

@Data
public class OrderQuery {
    private Long orderNo;
    private Integer status;
    private Date beginCreateTime;
    private Date endCreateTime;
    private Integer page;
    private Integer limit;
}
