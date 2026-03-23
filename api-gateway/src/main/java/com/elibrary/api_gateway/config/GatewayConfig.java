package com.elibrary.api_gateway.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class GatewayConfig {

    private static final List<String> PUBLIC_PATH_PREFIXES = List.of(
            "/api/v1/auth/",
            "/actuator/",
            "/swagger-ui/",
            "/v3/api-docs");

    @Bean
    public CorsWebFilter corsWebFilter(@Value("${app.cors.allowed-origin}") String allowedOrigin) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin(allowedOrigin);
        configuration.setAllowCredentials(true);
        configuration.addAllowedHeader(CorsConfiguration.ALL);
        configuration.addAllowedMethod(HttpMethod.GET);
        configuration.addAllowedMethod(HttpMethod.POST);
        configuration.addAllowedMethod(HttpMethod.PUT);
        configuration.addAllowedMethod(HttpMethod.PATCH);
        configuration.addAllowedMethod(HttpMethod.DELETE);
        configuration.addAllowedMethod(HttpMethod.OPTIONS);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return new CorsWebFilter(source);
    }

    @Bean
    public GlobalFilter authorizationHeaderFilter() {
        return (exchange, chain) -> {
            if (isPublicRequest(exchange) || hasAuthorizationHeader(exchange)) {
                return chain.filter(exchange);
            }

            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        };
    }

    private boolean isPublicRequest(ServerWebExchange exchange) {
        String path = exchange.getRequest().getURI().getPath();
        HttpMethod method = exchange.getRequest().getMethod();

        if (HttpMethod.OPTIONS.equals(method)) {
            return true;
        }

        return path.equals("/api/v1/auth")
                || path.equals("/actuator")
                || path.equals("/swagger-ui.html")
                || PUBLIC_PATH_PREFIXES.stream().anyMatch(path::startsWith);
    }

    private boolean hasAuthorizationHeader(ServerWebExchange exchange) {
        return StringUtils.hasText(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
    }
}
