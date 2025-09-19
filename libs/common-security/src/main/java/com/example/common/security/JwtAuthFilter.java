package com.example.common.security;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Minimal JWT header checker stub (replace with real JWT verification).
 */
@Component
public class JwtAuthFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String auth = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        // For skeleton, allow if header present; tighten later.
        if (auth == null || !auth.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }
        return chain.filter(exchange);
    }
}
