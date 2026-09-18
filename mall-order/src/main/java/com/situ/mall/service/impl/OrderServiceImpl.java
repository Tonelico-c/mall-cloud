package com.situ.mall.service.impl;

import com.situ.mall.api.cart.CartClient;
import com.situ.mall.api.pojo.vo.CartVO;
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
        order.setStatus(OrderStatus.UNPAID.getCode());
        order.setPaymentType(PaymentType.WECHAT.getCode()); // 微信支付
        order.setPostage(0); // 邮费为0
        List<CartVO> cartVOList = result.getData();
        List<CartVO> selectedCartVOList = cartVOList.stream()
                .filter(cartVO -> cartVO.getSelected() == 1)
                .toList();
        //把当先用户购物车里面勾选的这些商品插入order_item
        BigDecimal payment = BigDecimal.ZERO;
        for (CartVO cartVO : selectedCartVOList) {
            payment = payment.add(cartVO.getProduct().getPrice().multiply(BigDecimal.valueOf(cartVO.getCount())));
        }
        order.setPayment(payment);
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
        order.setPayment(payment);

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
