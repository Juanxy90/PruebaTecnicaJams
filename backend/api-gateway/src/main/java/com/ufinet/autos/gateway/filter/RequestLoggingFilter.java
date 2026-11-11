package com.ufinet.autos.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Filtro global: asigna un X-Request-Id (correlación entre microservicios)
 * y registra método, ruta, estado y duración de cada petición.
 */
@Slf4j
@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long start = System.currentTimeMillis();
        String requestId = exchange.getRequest().getHeaders().getFirst(REQUEST_ID_HEADER);
        String id = requestId != null ? requestId : UUID.randomUUID().toString();

        ServerHttpRequest request = exchange.getRequest().mutate().header(REQUEST_ID_HEADER, id).build();
        exchange.getResponse().getHeaders().set(REQUEST_ID_HEADER, id);

        return chain.filter(exchange.mutate().request(request).build())
                .doFinally(signal -> log.info("[{}] {} {} -> {} ({} ms)",
                        id,
                        request.getMethod(),
                        request.getURI().getPath(),
                        exchange.getResponse().getStatusCode(),
                        System.currentTimeMillis() - start));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
