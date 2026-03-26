package com.elibrary.user_service.controller;

import com.elibrary.user_service.config.RefreshTokenCookieService;
import com.elibrary.user_service.dto.LoginRequest;
import com.elibrary.user_service.dto.LoginResponse;
import com.elibrary.user_service.dto.RegisterRequest;
import com.elibrary.user_service.dto.UserResponse;
import com.elibrary.user_service.service.AuthSession;
import com.elibrary.user_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Public endpoints for user registration and login")
public class AuthController {

    private final UserService userService;
    private final RefreshTokenCookieService refreshTokenCookieService;

    public AuthController(UserService userService, RefreshTokenCookieService refreshTokenCookieService) {
        this.userService = userService;
        this.refreshTokenCookieService = refreshTokenCookieService;
    }

    @Operation(
        summary = "Register a new user account",
        description = "Creates a new user account. Role defaults to USER if not supplied. Password is stored as a BCrypt hash and is never returned."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Account created successfully",
            content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation failed - invalid email, weak password, or missing fields",
            content = @Content),
        @ApiResponse(responseCode = "409", description = "Email address is already registered",
            content = @Content)
    })
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
        summary = "Authenticate a user and issue a JWT",
        description = "Authenticates with email and password. On success returns a signed JWT and its expiry timestamp."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login successful",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content),
        @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
        @Valid @RequestBody LoginRequest request,
        HttpServletResponse httpServletResponse
    ) {
        AuthSession session = userService.login(request);
        refreshTokenCookieService.addRefreshTokenCookie(httpServletResponse, session.refreshToken());
        return ResponseEntity.ok(session.loginResponse());
    }

    @Operation(
        summary = "Issue a new JWT using a refresh token",
        description = "Reads the refresh token cookie and, when valid, rotates it and returns a new signed JWT."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Refresh successful",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))),
        @ApiResponse(responseCode = "401", description = "Missing, expired, or invalid refresh token", content = @Content)
    })
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
        HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse
    ) {
        String refreshToken = refreshTokenCookieService.extractRefreshToken(httpServletRequest);
        AuthSession session = userService.refresh(refreshToken);
        refreshTokenCookieService.addRefreshTokenCookie(httpServletResponse, session.refreshToken());
        return ResponseEntity.ok(session.loginResponse());
    }

    @Operation(
        summary = "Logout the current session",
        description = "Revokes the refresh token cookie for the current session and clears it from the client."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Logout successful"),
        @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content)
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
        HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse
    ) {
        String refreshToken = refreshTokenCookieService.extractRefreshToken(httpServletRequest);
        userService.logout(refreshToken);
        refreshTokenCookieService.clearRefreshTokenCookie(httpServletResponse);
        return ResponseEntity.noContent().build();
    }
}
