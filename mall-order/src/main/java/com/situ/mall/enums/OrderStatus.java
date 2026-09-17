package com.situ.mall.enums;

public enum OrderStatus {
    CANCELLED(0, "已取消"),
    UNPAID(1, "未支付"),
    PAID(2, "已支付"),
    DELIVERED(3, "已发货"),
    RECEIVED(4, "交易成功"),
    CLOSED(5, "已关闭");

    private int code;
    private String desc;

    OrderStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
