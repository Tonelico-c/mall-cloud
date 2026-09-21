package com.situ.mall.listener;

import com.situ.mall.constant.MqConstant;
import com.situ.mall.enums.OrderStatus;
import com.situ.mall.pojo.entity.Order;
import com.situ.mall.service.IOrderService;
import com.situ.mall.util.MultiDelayMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderStatusListener {
    @Autowired
    private IOrderService orderService;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(MqConstant.DELAY_ORDER_QUEUE),
            exchange = @Exchange(name = MqConstant.DELAY_EXCHANGE, delayed = "true"),
            key = MqConstant.DELAY_ORDER_ROUTING_KEY
    ))
    public void listenDelayMessage(MultiDelayMessage<Long> msg){
        // 1.获取消息中的订单号
        Long orderNo = msg.getData();
        log.info("收到订单延时消息, orderNo={}, 剩余延迟梯度={}", orderNo, msg.getDelayMillis());
        // 2.查询订单,判断其状态（已支付/已取消/已关闭都直接丢弃）
        Order order = orderService.getById(orderNo);
        if(order == null || !Integer.valueOf(OrderStatus.UNPAID.getCode()).equals(order.getStatus())){
            return;
        }
        // 3.订单仍未支付,还有延迟梯度就继续按下一档延时投递（用户中途付款,下一跳会自动短路）
        if(msg.hasNextDelay()){
            Long delay = msg.removeNextDelay();
            rabbitTemplate.convertAndSend(MqConstant.DELAY_EXCHANGE, MqConstant.DELAY_ORDER_ROUTING_KEY, msg, message -> {
                message.getMessageProperties().setDelay(delay.intValue());
                return message;
            });
            return;
        }
        // 4.延迟梯度已耗尽,取消订单并回补库存
        log.info("订单超时未支付,自动关单, orderNo={}", orderNo);
        orderService.cancelOrder(orderNo);
    }
}
