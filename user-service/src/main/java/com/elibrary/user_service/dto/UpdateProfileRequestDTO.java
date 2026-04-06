package com.elibrary.user_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Profile update request payload")
public class UpdateProfileRequestDTO {

    @Schema(description = "Updated email address for the authenticated user", example = "updated@elibrary.ie")
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid format")
    private String email;

    public UpdateProfileRequestDTO() {
    }

    public UpdateProfileRequestDTO(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
