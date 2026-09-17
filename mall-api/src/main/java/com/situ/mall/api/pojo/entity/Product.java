package com.situ.mall.api.pojo.entity;


import lombok.Data;

import java.math.BigDecimal;
@Data
public class Product {
    private Long id;
    private Long categoryId;
    private String name;
    private String subtitle;
    private String mainImage;
    private String subImages;
    private String detail;
    private BigDecimal price;
    private Integer stock;

}
