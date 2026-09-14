package com.situ.mall.utils;

import java.util.Map;

public class LoginContext {
    private final static ThreadLocal<Map<String, Object>> THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 设置当前线程的用户信息
     * @param map
     */
    public static void setLoginInfo(Map<String, Object> map) {
        THREAD_LOCAL.set(map);
    }
    /**
     * 获取当前线程的用户信息
     * @return
     */
    public static Map<String, Object> getLoginInfo() {
        return THREAD_LOCAL.get();
    }
    /**
     * 移除当前线程的用户信息
     */
    public static void removeLoginInfo() {
        THREAD_LOCAL.remove();
    }
}
