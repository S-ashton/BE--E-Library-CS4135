package com.elibrary.user_service.controller;

import com.elibrary.user_service.model.RefreshToken;
import com.elibrary.user_service.model.Role;
import com.elibrary.user_service.model.User;
import com.elibrary.user_service.repository.RefreshTokenRepository;
import com.elibrary.user_service.repository.UserRepository;
import com.elibrary.user_service.service.RefreshTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RefreshTokenIntegrationTest {

    private static final String LOGIN_URL = "/api/auth/login";
    private static final String REFRESH_URL = "/api/auth/refresh";
    private static final String LOGOUT_URL = "/api/auth/logout";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.jwt.refresh-cookie-name}")
    private String refreshCookieName;

    @BeforeEach
    void cleanDatabase() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Valid refresh cookie issues a new access token and rotates the refresh token")
    void refresh_validCookie_returnsNewAccessTokenAndRotatesRefreshCookie() throws Exception {
        userRepository.save(new User("reader@elibrary.ie", passwordEncoder.encode("Secure@123"), Role.USER));

        String originalRefreshToken = performLoginAndExtractRefreshToken();

        String refreshedSetCookie = mockMvc.perform(post(REFRESH_URL)
                .cookie(new Cookie(refreshCookieName, originalRefreshToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.expiresAt").isNotEmpty())
            .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString(refreshCookieName + "=")))
            .andReturn()
            .getResponse()
            .getHeader(HttpHeaders.SET_COOKIE);

        String rotatedRefreshToken = extractCookieValue(refreshedSetCookie);
        assertNotEquals(originalRefreshToken, rotatedRefreshToken);

        mockMvc.perform(post(REFRESH_URL)
                .cookie(new Cookie(refreshCookieName, originalRefreshToken)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Invalid refresh token"));
    }

    @Test
    @DisplayName("Missing refresh cookie returns 401")
    void refresh_missingCookie_returns401() throws Exception {
        mockMvc.perform(post(REFRESH_URL))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Invalid refresh token"));
    }

    @Test
    @DisplayName("Expired refresh token returns 401")
    void refresh_expiredToken_returns401() throws Exception {
        User user = userRepository.save(new User("reader@elibrary.ie", passwordEncoder.encode("Secure@123"), Role.USER));
        String refreshToken = performLoginAndExtractRefreshToken();

        refreshTokenRepository.deleteAll();
        refreshTokenRepository.save(new RefreshToken(
            user.getId(),
            refreshTokenService.hashToken(refreshToken),
            Instant.now().minusSeconds(5)
        ));

        mockMvc.perform(post(REFRESH_URL)
                .cookie(new Cookie(refreshCookieName, refreshToken)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Refresh token has expired"));
    }

    @Test
    @DisplayName("Logout revokes the refresh token and clears the cookie")
    void logout_revokesRefreshTokenAndClearsCookie() throws Exception {
        userRepository.save(new User("reader@elibrary.ie", passwordEncoder.encode("Secure@123"), Role.USER));
        String refreshToken = performLoginAndExtractRefreshToken();

        mockMvc.perform(post(LOGOUT_URL)
                .cookie(new Cookie(refreshCookieName, refreshToken)))
            .andExpect(status().isNoContent())
            .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.allOf(
                org.hamcrest.Matchers.containsString(refreshCookieName + "="),
                org.hamcrest.Matchers.containsString("Max-Age=0")
            )));

        mockMvc.perform(post(REFRESH_URL)
                .cookie(new Cookie(refreshCookieName, refreshToken)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Invalid refresh token"));
    }

    private String performLoginAndExtractRefreshToken() throws Exception {
        String setCookieHeader = mockMvc.perform(post(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "email", "reader@elibrary.ie",
                    "password", "Secure@123"
                ))))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getHeader(HttpHeaders.SET_COOKIE);

        return extractCookieValue(setCookieHeader);
    }

    private String extractCookieValue(String setCookieHeader) {
        String[] nameAndValue = setCookieHeader.split(";", 2)[0].split("=", 2);
        return nameAndValue[1];
    }
}
