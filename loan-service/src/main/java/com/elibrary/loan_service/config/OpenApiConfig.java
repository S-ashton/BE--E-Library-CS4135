package com.elibrary.loan_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI loanServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Loan Service API")
                        .description("Handles borrowing, returns, loan history, overdue detection, and notification task processing for the E-Library system.")
                        .version("v1"))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local via API Gateway"),
                        new Server().url("http://localhost:8083").description("Local direct")
                ));
    }
}