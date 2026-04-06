package com.elibrary.user_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Login request payload")
public class LoginRequest {

    @Schema(description = "Registered email address", example = "alice@elibrary.ie")
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid format")
    private String email;

    @Schema(description = "Account password", example = "Secure@123")
    @NotBlank(message = "Password is required")
    private String password;

    public LoginRequest() {}

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
