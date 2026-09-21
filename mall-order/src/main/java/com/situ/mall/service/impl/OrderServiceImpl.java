package com.situ.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.situ.mall.api.cart.CartClient;
import com.situ.mall.api.pojo.vo.CartVO;
import com.situ.mall.api.product.ProductClient;
import com.situ.mall.config.RabbitMQConfig;
import com.situ.mall.constant.MqConstant;
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
import com.situ.mall.util.MultiDelayMessage;
import com.situ.mall.utils.LoginContext;
import com.situ.mall.utils.Result;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.writer.UpdaterMapper;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Gao
 * @since 2026-09-17
 */
@Slf4j
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
    @Autowired
    private RabbitTemplate rabbitTemplate;

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

        // 发送延时消息,延时的总时长由延迟梯度累加得到,可按业务需要调整
        MultiDelayMessage<Long> multiDelayMessage  = new MultiDelayMessage<>(order.getOrderNo(),10000L, 10000L, 10000L, 15000L, 15000L, 30000L, 30000L);
        Long delay = multiDelayMessage .removeNextDelay();
        rabbitTemplate.convertAndSend(MqConstant.DELAY_EXCHANGE, MqConstant.DELAY_ORDER_ROUTING_KEY, multiDelayMessage, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                message.getMessageProperties().setDelay(delay.intValue());
                return message;
            }
        });
    }
    @Override
    public List<OrderVO> listItem() {
        Long userId = (Long) LoginContext.getLoginInfo().get("id");
        return orderMapper.listItem(userId);
    }

    /**
     * 取消订单并回补库存
     * 只允许把"未付款"状态的订单改成"已取消",以影响行数作为幂等闸门,
     * 保证无论消息重复投递多少次,关单和回补库存都只执行一次
     */
    @Override
    public void cancelOrder(Long orderNo) {
        // 1.带状态条件的更新,只有从"未付款"才能改成"已取消"
        UpdateWrapper<Order> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("order_no", orderNo)
                .eq("status", OrderStatus.UNPAID.getCode())
                .set("status", OrderStatus.CANCELLED.getCode())
                .set("close_time", new Date());
        int rows = orderMapper.update(updateWrapper);
        if (rows == 0) {
            // 订单不存在/已经被取消过/已经支付,直接结束,防止库存被重复回补
            return;
        }

        // 2.查出该订单的所有明细,一个订单可能包含多件商品
        List<OrderItem> orderItemList = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderNo, orderNo));

        // 3.逐条回补库存
        for (OrderItem orderItem : orderItemList) {
            try {
                Result<Void> stockResult = productClient.increaseStock(orderItem.getProductId(), orderItem.getQuantity());
                if (stockResult.getCode() != Result.OK) {
                    log.error("库存回补失败, orderNo={}, productId={}", orderNo, orderItem.getProductId());
                }
            } catch (Exception e) {
                // 关单的幂等闸门已经关闭,这里抛异常也补不回来,所以只记录日志,不影响其它商品回补
                log.error("库存回补异常, orderNo={}, productId={}", orderNo, orderItem.getProductId(), e);
            }
        }
    }
}
