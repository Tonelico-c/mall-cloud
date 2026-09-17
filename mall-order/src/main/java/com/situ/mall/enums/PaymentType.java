package com.situ.mall.enums;

public enum PaymentType {
    CASH_ON_DELIVERY(0, "货到付款"),
    WECHAT(1, "微信"),
    ALIPAY(2, "支付宝"),
    UNION_PAY(3, "银联支付");
    private int code;
    private String desc;

    PaymentType(int code, String desc) {
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
