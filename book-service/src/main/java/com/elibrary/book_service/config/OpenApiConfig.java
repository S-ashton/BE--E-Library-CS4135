package com.elibrary.book_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bookServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Book Service API")
                        .description("Handles book catalogue operations for the E-Library system.")
                        .version("v1"))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local via API Gateway"),
                        new Server().url("http://localhost:8082").description("Local direct")
                ));
    }
}