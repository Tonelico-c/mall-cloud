package com.situ.mall.filter;

import com.situ.mall.common.utils.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;


/**
 * 网关统一登录拦截：校验请求头里的 JWT
 * 网关是 WebFlux 响应式应用，不能用 HandlerInterceptor/WebMvcConfigurer，必须用 GlobalFilter
 */
@Component
public class LoginGlobalFilter implements GlobalFilter, Ordered {
    /**
     * 白名单：注意是【Vite 代理 rewrite 之后】的路径。
     * 前端请求 /api/admins/login，代理会把 /api 去掉，网关收到的是 /admins/login
     */
    private static final List<String> WHITE_LIST = List.of("/admins/login");
    /**
     * 401 响应体，结构等价于 Result.error("未登录或登录已过期")。
     * 前端 request.js 判断 401 时用的是自己的固定文案，这个 body 主要是给 curl/Postman 调试看的
     */
    private static final String UNAUTHORIZED_BODY = "{\"code\":401,\"msg\":\"未登录或登录已过期\"}";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 1. 白名单放行；OPTIONS 是跨域预检，本身不带 Authorization，必须放行
        if (WHITE_LIST.contains(path) || HttpMethod.OPTIONS.equals(request.getMethod())) {
            return chain.filter(exchange);
        }

        // 2. 取 token。前端 request.js 发的是裸 token，没有 "Bearer " 前缀，别照着网上的代码去截前缀
        String token = request.getHeaders().getFirst("Authorization");
        if (!StringUtils.hasText(token)) {
            return unauthorized(exchange);
        }

        // 3. 校验：过期、签名不对都会抛异常（java-jwt 的 JWTVerificationException 是运行时异常）
        try {
            Map<String, Object> map = JwtUtil.parseToken(token);

            // 4. 可选：把管理员id透传给下游服务。
            //    mutate() 会保留原始请求头，所以 Authorization 依然会传给下游（AdminController 还在用它手工解析）
            ServerHttpRequest newRequest = request.mutate()
                    .header("X-Admin-Id", String.valueOf(map.get("id")))
                    .build();
            return chain.filter(exchange.mutate().request(newRequest).build());
        } catch (Exception e) {
            return unauthorized(exchange);
        }
    }
    /** 未登录：返回 401 + JSON */
    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        DataBuffer buffer = response.bufferFactory()
                .wrap(UNAUTHORIZED_BODY.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        // 必须早于 NettyRoutingFilter（它的 order 是 Integer.MAX_VALUE），否则请求已经被转发到下游了
        return -100;
    }
}
