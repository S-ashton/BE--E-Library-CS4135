package com.elibrary.user_service.service;

import com.elibrary.user_service.config.JwtProperties;
import com.elibrary.user_service.exception.ExpiredRefreshTokenException;
import com.elibrary.user_service.exception.InvalidRefreshTokenException;
import com.elibrary.user_service.model.RefreshToken;
import com.elibrary.user_service.model.User;
import com.elibrary.user_service.repository.RefreshTokenRepository;
import com.elibrary.user_service.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class RefreshTokenService {

    private static final String INVALID_REFRESH_TOKEN_MESSAGE = "Invalid refresh token";
    private static final String EXPIRED_REFRESH_TOKEN_MESSAGE = "Refresh token has expired";

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtProperties jwtProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(
        RefreshTokenRepository refreshTokenRepository,
        UserRepository userRepository,
        JwtProperties jwtProperties
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.jwtProperties = jwtProperties;
    }

    @Transactional
    public String issueRefreshToken(User user) {
        String rawToken = generateRawToken();
        Instant expiresAt = Instant.now().plusSeconds(jwtProperties.getRefreshExpirationSeconds());
        RefreshToken refreshToken = new RefreshToken(user.getId(), hashToken(rawToken), expiresAt);
        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    @Transactional
    public AuthSession refreshSession(String rawRefreshToken, JwtService jwtService) {
        if (!StringUtils.hasText(rawRefreshToken)) {
            throw new InvalidRefreshTokenException(INVALID_REFRESH_TOKEN_MESSAGE);
        }

        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(hashToken(rawRefreshToken))
            .orElseThrow(() -> new InvalidRefreshTokenException(INVALID_REFRESH_TOKEN_MESSAGE));

        Instant now = Instant.now();
        if (storedToken.isRevoked()) {
            throw new InvalidRefreshTokenException(INVALID_REFRESH_TOKEN_MESSAGE);
        }

        if (storedToken.isExpired(now)) {
            storedToken.revoke(now);
            refreshTokenRepository.save(storedToken);
            throw new ExpiredRefreshTokenException(EXPIRED_REFRESH_TOKEN_MESSAGE);
        }

        User user = userRepository.findById(storedToken.getUserId())
            .filter(User::isActive)
            .orElseThrow(() -> new InvalidRefreshTokenException(INVALID_REFRESH_TOKEN_MESSAGE));

        storedToken.revoke(now);
        refreshTokenRepository.save(storedToken);

        String rotatedRefreshToken = issueRefreshToken(user);
        return new AuthSession(jwtService.generateAccessTokenResponse(user), rotatedRefreshToken);
    }

    @Transactional
    public void revoke(String rawRefreshToken) {
        if (!StringUtils.hasText(rawRefreshToken)) {
            return;
        }

        refreshTokenRepository.findByTokenHash(hashToken(rawRefreshToken)).ifPresent(token -> {
            if (!token.isRevoked()) {
                token.revoke(Instant.now());
                refreshTokenRepository.save(token);
            }
        });
    }

    public String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    private String generateRawToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
