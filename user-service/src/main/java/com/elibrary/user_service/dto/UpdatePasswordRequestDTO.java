package com.elibrary.user_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Password change request payload")
public class UpdatePasswordRequestDTO {

    @Schema(description = "Current password", example = "OldPass1!")
    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @Schema(description = "New password — must be at least 8 characters and contain an uppercase letter, a lowercase letter, a digit, and a special character", example = "NewPass1!")
    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "New password must be at least 8 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).+$",
        message = "New password must contain uppercase, lowercase, digit, and special character"
    )
    private String newPassword;

    public UpdatePasswordRequestDTO() {
    }

    public UpdatePasswordRequestDTO(String currentPassword, String newPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
