package com.situ.mall.pojo.vo;

import com.situ.mall.api.pojo.Product;
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
