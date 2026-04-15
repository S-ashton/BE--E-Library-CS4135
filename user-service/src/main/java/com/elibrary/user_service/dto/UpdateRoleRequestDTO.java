package com.elibrary.user_service.dto;

import com.elibrary.user_service.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Role assignment request payload")
public class UpdateRoleRequestDTO {

    @Schema(description = "Role to assign to the user", example = "STAFF", allowableValues = {"USER", "STAFF", "ADMIN"})
    @NotNull(message = "Role is required")
    private Role role;

    public UpdateRoleRequestDTO() {
    }

    public UpdateRoleRequestDTO(Role role) {
        this.role = role;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
