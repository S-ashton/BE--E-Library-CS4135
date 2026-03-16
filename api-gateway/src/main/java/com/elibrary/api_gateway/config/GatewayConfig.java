package com.elibrary.api_gateway.config;

import org.springframework.context.annotation.Configuration;

// Central configuration place for the API Gateway.
// Routing rules are in config-repo/api-gateway.yaml (deployed) and application-local.yaml (local development).
// OpenAPI metadata is in OpenApiConfig.java.
// Will add the following in the future: global auth filter, CORS config, rate limiter.
@Configuration
public class GatewayConfig {
    // This is intentionally empty the Spring Cloud Gateway picks up routes from 'spring.cloud.gateway.routes' automatically.
}
