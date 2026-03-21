package com.elibrary.user_service.repository;

import com.elibrary.user_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// Data access layer for User entities.
public interface UserRepository extends JpaRepository<User, Long> {

    // Used during registration to enforce email uniqueness.
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);
}
