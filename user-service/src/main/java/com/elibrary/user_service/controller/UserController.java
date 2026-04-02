package com.elibrary.user_service.controller;

import com.elibrary.user_service.dto.ProfileResponseDTO;
import com.elibrary.user_service.dto.UpdateProfileRequestDTO;
import com.elibrary.user_service.dto.UserResponse;
import com.elibrary.user_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Protected user profile endpoints")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(
        summary = "Get the current authenticated user",
        description = "Returns the profile for the authenticated user. Clients send a bearer token to the gateway; " +
            "the gateway validates it and forwards trusted identity to user-service.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Authenticated user profile",
            content = @Content(schema = @Schema(implementation = ProfileResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Missing, expired, or invalid token", content = @Content),
        @ApiResponse(responseCode = "404", description = "Authenticated user not found", content = @Content)
    })
    @GetMapping("/me")
    public ResponseEntity<ProfileResponseDTO> getCurrentUser(
        @Parameter(hidden = true)
        @RequestHeader("X-Authenticated-User-Id") Long authenticatedUserId
    ) {
        ProfileResponseDTO response = userService.getCurrentUser(authenticatedUserId);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Update the current authenticated user",
        description = "Updates permitted profile details for the authenticated user. Clients send a bearer token to the gateway; " +
            "the gateway validates it and forwards trusted identity to user-service.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Authenticated user profile updated",
            content = @Content(schema = @Schema(implementation = ProfileResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid update request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Missing, expired, or invalid token", content = @Content),
        @ApiResponse(responseCode = "404", description = "Authenticated user not found", content = @Content),
        @ApiResponse(responseCode = "409", description = "Updated email is already registered", content = @Content)
    })
    @PutMapping("/me")
    public ResponseEntity<ProfileResponseDTO> updateCurrentUser(
        @Parameter(hidden = true)
        @RequestHeader("X-Authenticated-User-Id") Long authenticatedUserId,
        @Valid @RequestBody UpdateProfileRequestDTO request
    ) {
        ProfileResponseDTO response = userService.updateCurrentUser(authenticatedUserId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "List all users",
        description = "Returns all user profiles. This endpoint is restricted to ADMIN users and is called through the gateway using a bearer token.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "All user profiles",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserResponse.class)))),
        @ApiResponse(responseCode = "401", description = "Missing, expired, or invalid token", content = @Content),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
}
