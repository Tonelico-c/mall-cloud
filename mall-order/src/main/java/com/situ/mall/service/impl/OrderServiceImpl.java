package com.situ.mall.service.impl;

import com.situ.mall.api.cart.CartClient;
import com.situ.mall.api.pojo.vo.CartVO;
import com.situ.mall.api.product.ProductClient;
import com.situ.mall.enums.OrderStatus;
import com.situ.mall.enums.PaymentType;
import com.situ.mall.exception.ServiceException;
import com.situ.mall.mapper.OrderItemMapper;
import com.situ.mall.pojo.entity.OrderItem;
import com.situ.mall.pojo.entity.Order;
import com.situ.mall.mapper.OrderMapper;
import com.situ.mall.pojo.vo.OrderVO;
import com.situ.mall.service.IOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.situ.mall.utils.LoginContext;
import com.situ.mall.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Gao
 * @since 2026-09-17
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private CartClient cartClient;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private ProductClient productClient;

    @Override
    @Transactional
    public void add(Order order) {
        Long userId = (Long) LoginContext.getLoginInfo().get("id");
        order.setUserId(userId);

        // 获取购物车中被选中的商品
        Result<List<CartVO>> result = cartClient.list();
        if(result.getCode() == Result.ERROR){
            throw new ServiceException("生成订单失败");
        }
        List<CartVO> cartVOList = result.getData();
        List<CartVO> selectedCartVOList = cartVOList.stream()
                .filter(cartVO -> cartVO.getProduct() != null && cartVO.getSelected() == 1)
                .toList();
        if(selectedCartVOList.isEmpty()){
            throw new ServiceException("购物车没有选中商品");
        }
        //把当先用户购物车里面勾选的这些商品插入order_item
        BigDecimal payment = BigDecimal.ZERO;
        for (CartVO cartVO : selectedCartVOList) {
            // 判断库存是否充足，如果充足，则继续相关业务，如果库存不足，抛出异常
            if(cartVO.getProduct().getStock() < cartVO.getCount()){
                throw new ServiceException("库存不足");
            }
            payment = payment.add(cartVO.getProduct().getPrice().multiply(BigDecimal.valueOf(cartVO.getCount())));
        }
        // 先减少商品的库存，然后再生成订单
        selectedCartVOList.forEach(cartVO -> {
            Result<Void> stockResult = productClient.decreaseStock(cartVO.getProductId(), cartVO.getCount());
            if(stockResult.getCode() != Result.OK){
                throw new ServiceException("库存不足");
            }
        });
        order.setPayment(payment);
        // 设置订单的一些基本信息
        order.setStatus(OrderStatus.UNPAID.getCode());
        order.setPaymentType(PaymentType.WECHAT.getCode()); // 微信支付
        order.setPostage(0); // 邮费为0
        orderMapper.insert(order);

        for (CartVO cartVO : selectedCartVOList) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderNo(order.getOrderNo());
            orderItem.setUserId(userId);
            orderItem.setProductId(cartVO.getProductId());
            orderItem.setProductName(cartVO.getProduct().getName());
            orderItem.setProductImage(cartVO.getProduct().getMainImage());
            orderItem.setCurrentUnitPrice(cartVO.getProduct().getPrice());
            orderItem.setQuantity(cartVO.getCount());
            // 计算总价
            BigDecimal productPrice = cartVO.getProduct().getPrice();
            BigDecimal quantity = BigDecimal.valueOf(cartVO.getCount());
            BigDecimal totalPrice = productPrice.multiply(quantity);
            orderItem.setTotalPrice(totalPrice);

            orderItemMapper.insert(orderItem);
        }

        //清除购物车已经下单的商品
        selectedCartVOList.forEach(cartVO -> {
            cartClient.deletedById(cartVO.getId());
        });
    }

    @Override
    public List<OrderVO> listItem() {
        Long userId = (Long) LoginContext.getLoginInfo().get("id");
        return orderMapper.listItem(userId);
    }
}
