package com.situ.mall.pojo.query;

import lombok.Data;

@Data
public class ProductQuery {
    private String name;
    private String subtitle;
    private Integer page;
    private Integer limit;
}
