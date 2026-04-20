package com.elibrary.api_gateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class GatewayConfig {

    private static final List<String> PUBLIC_PATH_PREFIXES = List.of(
            "/api/auth/",
            "/actuator/health",
            "/actuator/info",
            "/swagger-ui/",
            "/v3/api-docs");

    @Bean
    public GlobalFilter authorizationHeaderFilter(GatewayJwtService gatewayJwtService) {
        return (exchange, chain) -> {
            if (isPublicRequest(exchange)) {
                return chain.filter(exchange);
            }

            String authorizationHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (!StringUtils.hasText(authorizationHeader)) {
                return writeUnauthorizedResponse(exchange, "Authentication token is required");
            }

            if (!authorizationHeader.startsWith("Bearer ")) {
                return writeUnauthorizedResponse(exchange, "Invalid authentication token");
            }

            String token = authorizationHeader.substring(7).trim();
            if (!StringUtils.hasText(token)) {
                return writeUnauthorizedResponse(exchange, "Invalid authentication token");
            }

            try {
                Claims claims = gatewayJwtService.parseToken(token);
                String email = claims.getSubject();
                String role = claims.get("role", String.class);
                Object userId = claims.get("userId");

                if (!StringUtils.hasText(email) || !StringUtils.hasText(role) || userId == null) {
                    return writeUnauthorizedResponse(exchange, "Invalid authentication token");
                }

                ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(request -> request.headers(headers -> {
                        headers.remove(GatewayAuthenticationHeaders.AUTHENTICATED_USER);
                        headers.remove(GatewayAuthenticationHeaders.AUTHENTICATED_ROLE);
                        headers.remove(GatewayAuthenticationHeaders.AUTHENTICATED_USER_ID);
                        headers.set(GatewayAuthenticationHeaders.AUTHENTICATED_USER, email);
                        headers.set(GatewayAuthenticationHeaders.AUTHENTICATED_ROLE, role);
                        headers.set(GatewayAuthenticationHeaders.AUTHENTICATED_USER_ID, String.valueOf(userId));
                    }))
                    .build();

                return chain.filter(mutatedExchange);
            } catch (ExpiredJwtException ex) {
                return writeUnauthorizedResponse(exchange, "Token has expired");
            } catch (JwtException | IllegalArgumentException ex) {
                return writeUnauthorizedResponse(exchange, "Invalid authentication token");
            }
        };
    }

    private boolean isPublicRequest(ServerWebExchange exchange) {
        String path = exchange.getRequest().getURI().getPath();
        HttpMethod method = exchange.getRequest().getMethod();

        if (HttpMethod.OPTIONS.equals(method)) {
            return true;
        }

        return path.equals("/api/auth")
                || path.equals("/swagger-ui.html")
                || PUBLIC_PATH_PREFIXES.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> writeUnauthorizedResponse(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = """
            {"timestamp":"%s","status":401,"error":"Unauthorized","message":"%s"}
            """.formatted(Instant.now(), message).replace("\r", "").replace("\n", "");

        DataBuffer buffer = exchange.getResponse()
            .bufferFactory()
            .wrap(body.getBytes(StandardCharsets.UTF_8));

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}
