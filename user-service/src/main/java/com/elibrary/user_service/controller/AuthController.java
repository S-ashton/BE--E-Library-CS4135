package com.elibrary.user_service.controller;

import com.elibrary.user_service.dto.RegisterRequest;
import com.elibrary.user_service.dto.UserResponse;
import com.elibrary.user_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Handles public authentication endpoints, registration and for login in the future.
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Public endpoints for user registration and login")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @Operation(
        summary = "Register a new user account",
        description = "Creates a new user account. Role defaults to USER if not supplied. Password is stored as a BCrypt hash and is never returned."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Account created successfully",
            content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation failed — invalid email, weak password, or missing fields",
            content = @Content),
        @ApiResponse(responseCode = "409", description = "Email address is already registered",
            content = @Content)
    })
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
