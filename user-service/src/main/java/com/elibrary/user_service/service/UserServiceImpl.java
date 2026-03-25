package com.elibrary.user_service.service;

import com.elibrary.user_service.dto.LoginRequest;
import com.elibrary.user_service.dto.LoginResponse;
import com.elibrary.user_service.dto.ProfileResponseDTO;
import com.elibrary.user_service.dto.RegisterRequest;
import com.elibrary.user_service.dto.UpdateProfileRequestDTO;
import com.elibrary.user_service.dto.UserResponse;
import com.elibrary.user_service.exception.EmailAlreadyExistsException;
import com.elibrary.user_service.exception.UserNotFoundException;
import com.elibrary.user_service.model.User;
import com.elibrary.user_service.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public UserServiceImpl(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        AuthenticationManager authenticationManager,
        JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User(request.getEmail(), encodedPassword, request.getRole());

        User saved = userRepository.save(user);

        return UserResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken.unauthenticated(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        return jwtService.generateLoginResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponseDTO getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        return ProfileResponseDTO.from(user);
    }

    @Override
    @Transactional
    public ProfileResponseDTO updateCurrentUser(Long userId, UpdateProfileRequestDTO request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        String normalizedEmail = request.getEmail().trim();
        if (!normalizedEmail.equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException(normalizedEmail);
        }

        user.setEmail(normalizedEmail);
        User saved = userRepository.save(user);
        return ProfileResponseDTO.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
            .stream()
            .map(UserResponse::from)
            .toList();
    }
}
