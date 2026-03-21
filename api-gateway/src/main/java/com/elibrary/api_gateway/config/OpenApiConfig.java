package com.elibrary.api_gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

// Sets the OpenAPI spec metadata for the API Gateway.
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI gatewayOpenAPI() {
        // Documents the gateway itself as the single entry point for all client traffic.
        return new OpenAPI()
                .info(new Info()
                        .title("E-Library API Gateway")
                        .description(
                                "Single entry point for all E-Library client requests. " +
                                "Routes traffic to the correct downstream service - " +
                                "internal service URLs are never exposed to the client."
                        )
                        .version("v1"))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local development")
                ));
    }
}
