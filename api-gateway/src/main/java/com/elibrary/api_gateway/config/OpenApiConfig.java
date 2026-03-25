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
                                "and validates bearer tokens on protected routes. Internal service URLs " +
                                "are never exposed to the client."
                        )
                        .version("v1"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT bearer token sent to the gateway in the Authorization header.")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local development")
                ));
    }
}
