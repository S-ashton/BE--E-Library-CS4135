package com.elibrary.user_service.service;

import com.elibrary.user_service.config.JwtProperties;
import com.elibrary.user_service.dto.LoginResponse;
import com.elibrary.user_service.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JwtService {

    private final JwtProperties jwtProperties;
    private final SecretKey signingKey;
    private final JwtParser jwtParser;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.signingKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
        this.jwtParser = Jwts.parser()
            .verifyWith(signingKey)
            .requireIssuer(jwtProperties.getIssuer())
            .build();
    }

    public LoginResponse generateAccessTokenResponse(User user) {
        Instant issuedAt = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Instant expiresAt = issuedAt.plusSeconds(jwtProperties.getExpirationSeconds());

        String token = Jwts.builder()
            .subject(user.getEmail())
            .issuer(jwtProperties.getIssuer())
            .issuedAt(Date.from(issuedAt))
            .expiration(Date.from(expiresAt))
            .claim("userId", user.getId())
            .claim("email", user.getEmail())
            .claim("role", user.getRole().name())
            .signWith(signingKey)
            .compact();

        return new LoginResponse(token, expiresAt);
    }

    public LoginResponse generateLoginResponse(User user) {
        return generateAccessTokenResponse(user);
    }

    public Claims parseToken(String token) {
        return jwtParser.parseSignedClaims(token).getPayload();
    }
}
