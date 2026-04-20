package com.elibrary.user_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

// Inbound DTO for POST /api/auth/register.
// Password policy: min 8 chars, uppercase, lowercase, digit, and special character.
// Role defaults to USER if not included in the request.
@Schema(description = "Registration request payload")
public class RegisterRequest {

    @Schema(description = "Valid email address — must be unique", example = "alice@elibrary.ie")
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid format")
    private String email;

    @Schema(
        description = "Password meeting the strength policy: min 8 characters, uppercase, lowercase, digit, and special character",
        example = "Secure@123"
    )
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "Password must contain at least one uppercase letter, "
                + "one lowercase letter, one digit, and one special character (@$!%*?&)"
    )
    private String password;

    public RegisterRequest() {}

    public RegisterRequest(String email, String password) {
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
