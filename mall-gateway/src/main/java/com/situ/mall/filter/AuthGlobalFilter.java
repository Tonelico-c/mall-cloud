package com.situ.mall.filter;

import com.situ.mall.util.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;


/**
 * 网关统一登录拦截：校验请求头里的 JWT
 * 网关是 WebFlux 响应式应用，不能用 HandlerInterceptor/WebMvcConfigurer，必须用 GlobalFilter
 */
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 白名单路径（比如 /login、/register 不需要校验）
        if (path.startsWith("/service/captcha")
                || path.startsWith("/admin/login")
                || path.startsWith("/user/login")) {
            return chain.filter(exchange);
        }

        // 取 token。前端 request.js 发的是裸 token，没有 "Bearer " 前缀
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");

        // 校验：过期、签名不对都会抛异常（java-jwt 的 JWTVerificationException 是运行时异常）
        try {
            //令牌是伪造的或者已经过期都会抛出异常
            Map<String, Object> map = JwtUtil.parseToken(token);
            // 继续往下游传递用户信息
            ServerHttpRequest request = exchange.getRequest()
                    .mutate()
                    .header("X-Login-Id", String.valueOf(map.get("id")))
                    .header("X-Login-Name", String.valueOf(map.get("name")))
                    .build();
            return chain.filter(exchange.mutate().request(request).build());
        } catch (Exception e) {
            //http的响应状态改成401
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }
    @Override
    public int getOrder() {
        return 0;
    }
}
