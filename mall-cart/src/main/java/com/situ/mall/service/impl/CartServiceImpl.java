package com.situ.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.situ.mall.api.pojo.entity.Product;
import com.situ.mall.api.product.ProductClient;
import com.situ.mall.exception.ServiceException;
import com.situ.mall.pojo.entity.Cart;
import com.situ.mall.mapper.CartMapper;
import com.situ.mall.pojo.vo.CartVO;
import com.situ.mall.service.ICartService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.situ.mall.utils.LoginContext;
import com.situ.mall.utils.Result;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    @Autowired
    private ProductClient productClient;
    @Override
    @Transactional
    public void add(Cart cart) {
        if(cart.getCount() == null || cart.getCount() < 1){
            throw new ServiceException("购买数量不合法!!!");
        }
        // 获取当前登录用户id
        Long userId = (Long) LoginContext.getLoginInfo().get("id");
        cart.setUserId(userId);
        // 根据用户id和商品id查询购物车
        LambdaQueryWrapper<Cart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Cart::getUserId, userId)
                     .eq(Cart::getProductId, cart.getProductId());
        Cart dbCart = cartMapper.selectOne(queryWrapper);
        // 判断加入购物车的商品的库存是否足够
        Product product = productClient.selectById(cart.getProductId()).getData();
        if(product == null){
            throw new ServiceException("商品不存在");
        }
        int productStock = (dbCart == null) ? 0 : dbCart.getCount();
        if (product.getStock() < productStock + cart.getCount()){
            throw new ServiceException("库存不足");
        }
        if (dbCart != null) {
            // 购物车已存在，更新数量
            dbCart.setCount(dbCart.getCount() + cart.getCount());
            cartMapper.updateById(dbCart);
        } else {
            // 购物车不存在，新增
            cartMapper.insert(cart);
        }
    }

    @Override
    public List<CartVO> listVO() {
        Long userId = (Long) LoginContext.getLoginInfo().get("id");
        List<Cart> cartList = cartMapper.selectList(new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId));
        List<CartVO> cartVOList = cartList.stream().map(cart -> {
            CartVO cartVO = new CartVO();
            BeanUtils.copyProperties(cart, cartVO);
            // 远程调用获取商品信息
            Result<Product> productResult = productClient.selectById(cart.getProductId());
            if (productResult.getCode() == Result.OK) {
                cartVO.setProduct(productResult.getData());
            }
            return cartVO;
        }).toList();
        return cartVOList;
    }

    @Override
    public void update(Cart cart) {
        Long userId = (Long) LoginContext.getLoginInfo().get("id");
        cart.setUserId(userId);
        cartMapper.updateById(cart);
    }
}
