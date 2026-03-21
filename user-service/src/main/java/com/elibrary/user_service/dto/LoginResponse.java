package com.elibrary.user_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Login response payload")
public class LoginResponse {

    @Schema(description = "Signed JWT bearer token")
    private String token;

    @Schema(description = "UTC timestamp when the token expires")
    private Instant expiresAt;

    public LoginResponse() {}

    public LoginResponse(String token, Instant expiresAt) {
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public String getToken() {
        return token;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
