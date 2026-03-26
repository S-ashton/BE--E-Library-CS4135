package com.elibrary.user_service.config;

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
    public OpenAPI userServiceOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT access token used by clients when calling protected endpoints through the API Gateway. " +
                                                "The gateway validates the token and forwards trusted identity to user-service."))
                        .addSecuritySchemes("refreshCookieAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.COOKIE)
                                        .name("refreshToken")
                                        .description("HttpOnly refresh-token cookie used by the refresh and logout endpoints.")))
                .info(new Info()
                        .title("User Service API")
                        .description("Handles user registration, authentication, and profile management for the E-Library system. " +
                                "Login returns a JWT access token in the response body and sets a refresh token in an HttpOnly cookie. " +
                                "Protected endpoints are normally accessed through the API Gateway, which validates bearer tokens " +
                                "before forwarding trusted identity headers to user-service. Refresh and logout rely on the refresh-token cookie.")
                        .version("v1"))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local API Gateway URL preferred for client-facing testing"),
                        new Server().url("http://localhost:8081").description("Local internal user-service URL")
                ));
    }
}
