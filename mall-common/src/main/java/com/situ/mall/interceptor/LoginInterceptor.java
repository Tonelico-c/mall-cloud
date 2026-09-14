package com.situ.mall.interceptor;

import com.situ.mall.utils.LoginContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashMap;
import java.util.Map;

public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String id = request.getHeader("X-Login-Id");
        String name = request.getHeader("X-Login-Name");
        // 2.判断是否为空
        if (StringUtils.hasText(id) && StringUtils.hasText(name)) {
            // 不为空，保存到ThreadLocal
            Map<String, Object> map = new HashMap<>();
            map.put("id", Long.valueOf(id));
            map.put("name", name);
            LoginContext.setLoginInfo(map);
        }
        // 3.放行
        return true;
    }
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 移除用户
        LoginContext.removeLoginInfo();
    }
}
