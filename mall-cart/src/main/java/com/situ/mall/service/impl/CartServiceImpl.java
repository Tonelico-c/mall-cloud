package com.situ.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.situ.mall.pojo.entity.Cart;
import com.situ.mall.mapper.CartMapper;
import com.situ.mall.service.ICartService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.situ.mall.utils.LoginContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Gao
 * @since 2026-09-16
 */
@Service
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements ICartService {

    @Autowired
    private CartMapper cartMapper;
    @Override
    public void add(Cart cart) {
        // 获取当前登录用户id
        Long userId = (Long) LoginContext.getLoginInfo().get("id");
        cart.setUserId(userId);

        LambdaQueryWrapper<Cart> queryWrapper = new LambdaQueryWrapper<>();
        // 根据用户id和商品id查询购物车
        queryWrapper.eq(Cart::getUserId, userId)
                     .eq(Cart::getProductId, cart.getProductId());
        Cart dbCart = cartMapper.selectOne(queryWrapper);
        if (dbCart != null) {
            // 购物车已存在，更新数量
            dbCart.setCount(dbCart.getCount() + cart.getCount());
            cartMapper.updateById(dbCart);
        } else {
            // 购物车不存在，新增
            cartMapper.insert(cart);
        }
    }
}
