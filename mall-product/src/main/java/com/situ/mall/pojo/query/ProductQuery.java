package com.situ.mall.pojo.query;

import lombok.Data;

@Data
public class ProductQuery {
    private String name;
    private String subtitle;
    private Long categoryId;
    private Integer page;
    private Integer limit;
}
