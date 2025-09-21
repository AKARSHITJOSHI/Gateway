package com.akarshit.gateway.filter;

import com.akarshit.gateway.router.ConsistentHashRouter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.cloud.gateway.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
public class ConsistentHashFilter implements GlobalFilter, Ordered {

    private final ConsistentHashRouter router;

    public ConsistentHashFilter(ConsistentHashRouter router) {
        this.router = router;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String key = exchange.getRequest().getQueryParams().getFirst("key");
        if (key == null) {
            return chain.filter(exchange);
        }

        String targetNode = router.getNode(key);
        if (targetNode == null) {
            return chain.filter(exchange); // fallback if no nodes
        }

        URI newUri = URI.create(targetNode + exchange.getRequest().getURI().getPath());
        ServerHttpRequest newRequest = exchange.getRequest().mutate().uri(newUri).build();

        return chain.filter(exchange.mutate().request(newRequest).build());
    }

    @Override
    public int getOrder() {
        return -1;
    }
}

