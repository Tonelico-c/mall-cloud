package com.situ.mall.util;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class MultiDelayMessage<T> implements Serializable {

    /**
     * 消息体 订单的orderNo
     */
    private T data;

    /**
     * 记录延时时间的集合
     */
    private List<Long> delayMillis;

    public MultiDelayMessage() {
    }

    public MultiDelayMessage(T data, List<Long> delayMillis) {
        this.data = data;
        this.delayMillis = delayMillis;
    }

    public MultiDelayMessage(T data, Long ...delayMillis) {
        this.data = data;
        //this.delayMillis = Arrays.asList(delayMillis);
        List<Long> list = new ArrayList<>();
        for (Long delayMilli : delayMillis) {
            list.add(delayMilli);
        }
        this.delayMillis = list;

    }

    /**
     * 获取并移除下一个延迟时间
     * @return 集合中第一个延迟时间
     */
    public Long removeNextDelay() {
        return delayMillis.remove(0);
    }

    /**
     * 是否有下一个延迟时间
     * @return
     */
    public boolean hasNextDelay() {
        return !delayMillis.isEmpty();
    }
}
