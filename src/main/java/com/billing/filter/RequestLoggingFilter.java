package com.billing.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * WebFilter = the WebFlux version of a servlet Filter. Runs around every request:
 *  - makes sure each request has an X-Request-Id (and echoes it back)
 *  - logs method, path, status and how long the request took
 */
@Slf4j
@Component
public class RequestLoggingFilter implements WebFilter {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String incoming = exchange.getRequest().getHeaders().getFirst(REQUEST_ID_HEADER);
        String requestId = (incoming == null || incoming.isBlank()) ? UUID.randomUUID().toString() : incoming;
        exchange.getResponse().getHeaders().set(REQUEST_ID_HEADER, requestId);

        long start = System.nanoTime();
        return chain.filter(exchange)
                .doFinally(signal -> log.info("[{}] {} {} -> {} in {} ms",
                        requestId,
                        exchange.getRequest().getMethod(),
                        exchange.getRequest().getPath(),
                        exchange.getResponse().getStatusCode(),
                        (System.nanoTime() - start) / 1_000_000));
    }
}
