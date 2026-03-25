package com.elibrary.user_service.service;

import com.elibrary.user_service.dto.LoginRequest;
import com.elibrary.user_service.dto.LoginResponse;
import com.elibrary.user_service.dto.ProfileResponseDTO;
import com.elibrary.user_service.dto.RegisterRequest;
import com.elibrary.user_service.dto.UpdateProfileRequestDTO;
import com.elibrary.user_service.dto.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    ProfileResponseDTO getCurrentUser(Long userId);

    ProfileResponseDTO updateCurrentUser(Long userId, UpdateProfileRequestDTO request);

    List<UserResponse> getAllUsers();
}
