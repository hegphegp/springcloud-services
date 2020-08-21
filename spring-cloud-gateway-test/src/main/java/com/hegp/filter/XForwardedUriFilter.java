package com.hegp.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class XForwardedUriFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        // 放行websocket的URL, sockjs发了两个请求, 一个是http协议开头的, 一个是ws协议开头
        // 放行sockjs发的http协议请求, 并设置 ServerWebExchangeUtils.setAlreadyRouted(exchange); , 不再进入后面的路由校验
        // 后续还有限流的过滤器, 防爬虫的过滤器, 所以不设置 ServerWebExchangeUtils.setAlreadyRouted(exchange);
        String path = request.getURI().getPath();
        // 放行sockjs发的ws协议开头
        if (path.startsWith("/websocket/ws/")) {
//            是websocket请求时, 不能设置 ServerWebExchangeUtils.setAlreadyRouted(exchange); 否则进入不了WebsocketRoutingFilter, 导致websocket转发出问题
//            ServerWebExchangeUtils.setAlreadyRouted(exchange);
            return chain.filter(exchange);
        } else {
            // 如果网关前面经过nginx,nginx一定要配置 proxy_set_header X-Forwarded-Uri $uri; , 否则后面的服务取不到完整的请求URL
            // 如果网关服务前面没有nginx的代理,网关一定要配置 "x-forwarded-uri" 参数, 否则后面的服务取不到完整的请求URL
            if (request.getHeaders().containsKey("x-forwarded-uri") == false) { // 请求头有 x-forwarded-uri 参数,不添加; 没有, 才添加
                //向headers中放文件，记得build
                ServerHttpRequest host = request.mutate().header("x-forwarded-uri", new String[]{request.getURI().getPath()}).build();
                //将现在的request 变成 change对象
                ServerWebExchange build = exchange.mutate().request(host).build();
                return chain.filter(build);
            } else {
                return chain.filter(exchange);
            }
        }
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
