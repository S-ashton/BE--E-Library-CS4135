package com.elibrary.user_service.service;

import com.elibrary.user_service.dto.LoginRequest;
import com.elibrary.user_service.dto.ProfileResponseDTO;
import com.elibrary.user_service.dto.RegisterRequest;
import com.elibrary.user_service.dto.UpdatePasswordRequestDTO;
import com.elibrary.user_service.dto.UpdateProfileRequestDTO;
import com.elibrary.user_service.dto.UpdateRoleRequestDTO;
import com.elibrary.user_service.dto.UserResponse;
import com.elibrary.user_service.exception.EmailAlreadyExistsException;
import com.elibrary.user_service.exception.IncorrectPasswordException;
import com.elibrary.user_service.exception.UserNotFoundException;
import com.elibrary.user_service.messaging.UserDeletedEvent;
import com.elibrary.user_service.messaging.UserEmailUpdatedEvent;
import com.elibrary.user_service.messaging.UserEventPublisher;
import com.elibrary.user_service.model.User;
import com.elibrary.user_service.repository.RefreshTokenRepository;
import com.elibrary.user_service.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final Optional<UserEventPublisher> userEventPublisher;

    public UserServiceImpl(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        AuthenticationManager authenticationManager,
        JwtService jwtService,
        RefreshTokenService refreshTokenService,
        RefreshTokenRepository refreshTokenRepository,
        Optional<UserEventPublisher> userEventPublisher
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userEventPublisher = userEventPublisher;
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
    @Transactional
    public AuthSession login(LoginRequest request) {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken.unauthenticated(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        String refreshToken = refreshTokenService.issueRefreshToken(user);
        return new AuthSession(jwtService.generateAccessTokenResponse(user), refreshToken);
    }

    @Override
    @Transactional
    public AuthSession refresh(String refreshToken) {
        return refreshTokenService.refreshSession(refreshToken, jwtService);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.revoke(refreshToken);
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
        boolean emailChanged = !normalizedEmail.equalsIgnoreCase(user.getEmail());

        if (emailChanged && userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException(normalizedEmail);
        }

        user.setEmail(normalizedEmail);
        User saved = userRepository.save(user);

        if (emailChanged) {
            userEventPublisher.ifPresent(pub -> pub.publishUserEmailUpdated(
                new UserEmailUpdatedEvent(saved.getId(), saved.getEmail(), Instant.now())
            ));
        }

        return ProfileResponseDTO.from(saved);
    }

    @Override
    @Transactional
    public void updatePassword(Long userId, UpdatePasswordRequestDTO request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IncorrectPasswordException();
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserResponse updateUserRole(Long targetUserId, UpdateRoleRequestDTO request) {
        User user = userRepository.findById(targetUserId)
            .orElseThrow(() -> new UserNotFoundException(targetUserId));

        user.setRole(request.getRole());
        User saved = userRepository.save(user);
        return UserResponse.from(saved);
    }

    @Override
    @Transactional
    public void deleteUser(Long targetUserId) {
        User user = userRepository.findById(targetUserId)
            .orElseThrow(() -> new UserNotFoundException(targetUserId));

        refreshTokenRepository.deleteByUserId(targetUserId);
        userRepository.delete(user);

        userEventPublisher.ifPresent(pub -> pub.publishUserDeleted(
            new UserDeletedEvent(targetUserId, user.getEmail(), Instant.now())
        ));
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

