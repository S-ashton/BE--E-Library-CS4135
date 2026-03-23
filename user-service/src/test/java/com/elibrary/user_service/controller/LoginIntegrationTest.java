package com.elibrary.user_service.controller;

import com.elibrary.user_service.model.Role;
import com.elibrary.user_service.model.User;
import com.elibrary.user_service.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LoginIntegrationTest {

    private static final String LOGIN_URL = "/api/v1/auth/login";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Valid credentials return a JWT and expiry without sensitive fields")
    void login_validCredentials_returnsJwtAndExpiry() throws Exception {
        userRepository.save(new User("reader@elibrary.ie", passwordEncoder.encode("Secure@123"), Role.USER));

        String responseBody = mockMvc.perform(post(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "email", "reader@elibrary.ie",
                    "password", "Secure@123"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value(notNullValue()))
            .andExpect(jsonPath("$.expiresAt").value(notNullValue()))
            .andExpect(jsonPath("$.password").doesNotExist())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String token = objectMapper.readTree(responseBody).get("token").asText();
        Instant expiresAt = Instant.parse(objectMapper.readTree(responseBody).get("expiresAt").asText());

        Claims claims = Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
            .build()
            .parseSignedClaims(token)
            .getPayload();

        assertEquals("reader@elibrary.ie", claims.getSubject());
        assertEquals("USER", claims.get("role", String.class));
        assertEquals(claims.getExpiration().toInstant(), expiresAt);
    }

    @Test
    @DisplayName("Invalid password returns a generic 401 response")
    void login_invalidPassword_returnsGeneric401() throws Exception {
        userRepository.save(new User("reader@elibrary.ie", passwordEncoder.encode("Secure@123"), Role.USER));

        mockMvc.perform(post(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "email", "reader@elibrary.ie",
                    "password", "WrongPassword@123"
                ))))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.message").value("Invalid credentials"))
            .andExpect(jsonPath("$.message", not(containsString("password"))));
    }

    @Test
    @DisplayName("Unknown email returns the same generic 401 response")
    void login_unknownEmail_returnsGeneric401() throws Exception {
        mockMvc.perform(post(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "email", "missing@elibrary.ie",
                    "password", "Secure@123"
                ))))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.message").value("Invalid credentials"))
            .andExpect(jsonPath("$.message", not(containsString("missing@elibrary.ie"))));
    }
}
