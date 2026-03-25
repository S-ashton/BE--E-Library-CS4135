package com.elibrary.user_service.dto;

import com.elibrary.user_service.model.Role;
import com.elibrary.user_service.model.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Authenticated user profile response with sensitive fields excluded")
public class ProfileResponseDTO {

    @Schema(description = "User ID", example = "1")
    private Long id;

    @Schema(description = "User email address", example = "alice@elibrary.ie")
    private String email;

    @Schema(description = "User role", example = "USER")
    private Role role;

    @Schema(description = "Account creation timestamp (UTC)")
    private Instant createdAt;

    public ProfileResponseDTO() {
    }

    public ProfileResponseDTO(Long id, String email, Role role, Instant createdAt) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
    }

    public static ProfileResponseDTO from(User user) {
        return new ProfileResponseDTO(
            user.getId(),
            user.getEmail(),
            user.getRole(),
            user.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
