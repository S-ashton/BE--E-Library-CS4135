package com.elibrary.user_service.service;

import com.elibrary.user_service.dto.LoginResponse;

public record AuthSession(LoginResponse loginResponse, String refreshToken) {
}
