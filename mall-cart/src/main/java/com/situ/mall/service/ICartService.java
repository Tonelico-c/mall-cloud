package com.situ.mall.service;

import com.situ.mall.pojo.entity.Cart;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Gao
 * @since 2026-09-16
 */
public interface ICartService extends IService<Cart> {

    void add(Cart cart);
}
