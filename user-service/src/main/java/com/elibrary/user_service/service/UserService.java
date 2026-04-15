package com.elibrary.user_service.service;

import com.elibrary.user_service.dto.LoginRequest;
import com.elibrary.user_service.dto.ProfileResponseDTO;
import com.elibrary.user_service.dto.RegisterRequest;
import com.elibrary.user_service.dto.UpdatePasswordRequestDTO;
import com.elibrary.user_service.dto.UpdateProfileRequestDTO;
import com.elibrary.user_service.dto.UpdateRoleRequestDTO;
import com.elibrary.user_service.dto.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse register(RegisterRequest request);

    AuthSession login(LoginRequest request);

    AuthSession refresh(String refreshToken);

    void logout(String refreshToken);

    ProfileResponseDTO getCurrentUser(Long userId);

    ProfileResponseDTO updateCurrentUser(Long userId, UpdateProfileRequestDTO request);

    void updatePassword(Long userId, UpdatePasswordRequestDTO request);

    UserResponse updateUserRole(Long targetUserId, UpdateRoleRequestDTO request);

    void deleteUser(Long targetUserId);

    List<UserResponse> getAllUsers();
}

