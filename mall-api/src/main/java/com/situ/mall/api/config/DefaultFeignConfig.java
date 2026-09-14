package com.situ.mall.api.config;

import com.situ.mall.utils.LoginContext;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class DefaultFeignConfig {

    @Bean
    public RequestInterceptor userInfoRequestInterceptor(){
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                // 获取登录用户
                Long id = (Long) LoginContext.getLoginInfo().get("id");;
                String name = (String) LoginContext.getLoginInfo().get("name");;
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
