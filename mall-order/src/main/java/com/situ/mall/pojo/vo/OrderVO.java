package com.situ.mall.pojo.vo;

import com.situ.mall.pojo.entity.Order;
import com.situ.mall.pojo.entity.OrderItem;
import lombok.Data;

import java.util.List;

@Data
public class OrderVO extends Order {
    List<OrderItem> orderItemList;
}
