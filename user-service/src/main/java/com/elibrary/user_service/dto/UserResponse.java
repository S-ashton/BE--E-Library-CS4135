package com.elibrary.user_service.dto;

import com.elibrary.user_service.model.Role;
import com.elibrary.user_service.model.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

// Outbound DTO returned after successful registration.
// Password and other sensitive fields are intentionally excluded.
@Schema(description = "Registration confirmation response — password and sensitive fields are excluded")
public class UserResponse {

    @Schema(description = "Generated account ID", example = "1")
    private Long id;

    @Schema(description = "Registered email address", example = "alice@elibrary.ie")
    private String email;

    @Schema(description = "Role assigned to the account", example = "USER")
    private Role role;

    @Schema(description = "Account creation timestamp (UTC)")
    private Instant createdAt;

    public UserResponse() {}

    public UserResponse(Long id, String email, Role role, Instant createdAt) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
    }

    public static UserResponse from(User user) {
        return new UserResponse(
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
