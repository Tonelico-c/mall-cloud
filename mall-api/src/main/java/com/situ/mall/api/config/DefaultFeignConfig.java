package com.situ.mall.api.config;

import com.situ.mall.utils.LoginContext;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.Map;

@Configuration
public class DefaultFeignConfig {

    @Bean
    public RequestInterceptor userInfoRequestInterceptor(){
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                // 获取登录用户
                Map<String, Object> loginInfo = LoginContext.getLoginInfo();
                /*
                 * 必须判空：LoginContext 底层是 ThreadLocal，只有经过 LoginInterceptor 的 Web 请求线程才有值。
                 * 定时任务线程（@Scheduled）和 MQ 消费者线程都没有请求上下文，这里拿到的是 null。
                 * 不判空的话下面 .get("id") 会抛 NPE，后果是：
                 *   1. DeleteImageJob 每天执行时在第一次 Feign 调用就中断，图片清理功能静默失效；
                 *   2. 订单超时关单后回补库存的 Feign 调用失败，异常被 catch 住只打了日志，
                 *      表现为"订单正常取消、库存却没还回去"。
                 * 这些场景调用的接口（查所有图片、增加库存）本身也不依赖登录用户，直接跳过即可。
                 */
                if (loginInfo == null) {
                    return;
                }
                Object id = loginInfo.get("id");
                String name = (String) loginInfo.get("name");
                if(id == null || !StringUtils.hasText(name)) {
                    // 如果为空则直接跳过
                    return;
                }
                // 如果不为空则放入请求头中，传递给下游微服务
                template.header("X-Login-Id", String.valueOf(id));
                template.header("X-Login-Name", name);
            }
        };
    }
}
