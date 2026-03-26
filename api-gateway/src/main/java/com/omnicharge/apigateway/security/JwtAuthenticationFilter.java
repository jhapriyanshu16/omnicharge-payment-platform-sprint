package com.omnicharge.apigateway.security;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        // Allow public auth routes
        if (path.contains("/auth/")) {
            return chain.filter(exchange);
        }

        String authHeader =
                exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = jwtUtil.validate(token);

            String email = claims.getSubject();
            String role = claims.get("role", String.class);

            HttpMethod method = exchange.getRequest().getMethod();

            // ADMIN Only Operations
            if (path.contains("/operators")
                    && (method == HttpMethod.POST || method == HttpMethod.DELETE)) {

                if (!"ROLE_ADMIN".equals(role)) {
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
            }
            if (path.contains("/plans")
                    && (method == HttpMethod.POST || method == HttpMethod.DELETE)) {

                if (!"ROLE_ADMIN".equals(role)) {
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
            }

            // Propagate identity headers
            ServerHttpRequest request = exchange.getRequest()
                    .mutate()
                    .header("X-User-Email", email)
                    .header("X-User-Role", role)
                    .build();

            return chain.filter(exchange.mutate().request(request).build());

        } catch (Exception ex) {

            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -1;
    }
}