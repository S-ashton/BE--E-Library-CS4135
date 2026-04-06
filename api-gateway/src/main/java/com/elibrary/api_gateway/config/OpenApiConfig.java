package com.elibrary.api_gateway.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI gatewayOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("E-Library API Gateway")
                        .description(
                                "Single entry point for all E-Library client requests. " +
                                "Routes traffic to the correct downstream service, applies CORS, " +
                                "validates JWT bearer access tokens on protected routes, and exposes " +
                                "public authentication endpoints under /api/auth. Login returns an access token " +
                                "in the response body and sets a refresh token in an HttpOnly cookie. " +
                                "Refresh and logout use that refresh-token cookie."
                        )
                        .version("v1"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT access token sent to the gateway in the Authorization header as 'Bearer <token>'."))
                        .addSecuritySchemes("refreshCookieAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.COOKIE)
                                        .name("refreshToken")
                                        .description("HttpOnly refresh-token cookie used by /api/auth/refresh and /api/auth/logout.")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local development")
                ));
    }
}
