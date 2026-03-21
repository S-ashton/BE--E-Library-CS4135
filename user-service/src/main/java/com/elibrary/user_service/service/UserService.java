package com.elibrary.user_service.service;

import com.elibrary.user_service.dto.RegisterRequest;
import com.elibrary.user_service.dto.UserResponse;

// Business logic contract for user account management.
public interface UserService {

    // Registers a new user. Throws EmailAlreadyExistsException if the email is taken.
    UserResponse register(RegisterRequest request);
}
