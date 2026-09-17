package com.situ.mall.api.pojo.vo;

import com.situ.mall.api.pojo.entity.Product;
import lombok.Data;

@Data
public class CartVO{
    private Long id;
    private Long userId;
    private Long productId;
    private Integer count;
    private Integer selected;
    private Product product;
}