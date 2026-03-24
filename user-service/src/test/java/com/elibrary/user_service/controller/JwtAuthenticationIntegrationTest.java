package com.elibrary.user_service.controller;

import com.elibrary.user_service.dto.LoginResponse;
import com.elibrary.user_service.model.Role;
import com.elibrary.user_service.model.User;
import com.elibrary.user_service.repository.UserRepository;
import com.elibrary.user_service.service.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
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

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class JwtAuthenticationIntegrationTest {

    private static final String CURRENT_USER_URL = "/api/users/me";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.issuer}")
    private String jwtIssuer;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Protected endpoint without token returns standardised 401 JSON")
    void me_withoutToken_returnsJson401() throws Exception {
        mockMvc.perform(get(CURRENT_USER_URL))
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.timestamp").value(notNullValue()))
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.error").value("Unauthorized"))
            .andExpect(jsonPath("$.message").value("Authentication token is required"));
    }

    @Test
    @DisplayName("Protected endpoint with expired token returns standardised 401 JSON")
    void me_withExpiredToken_returnsJson401() throws Exception {
        User savedUser = userRepository.save(new User("expired@elibrary.ie", passwordEncoder.encode("Secure@123"), Role.USER));
        String expiredToken = buildExpiredToken(savedUser);

        mockMvc.perform(get(CURRENT_USER_URL)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken))
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.timestamp").value(notNullValue()))
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.error").value("Unauthorized"))
            .andExpect(jsonPath("$.message").value("Token has expired"));
    }

    @Test
    @DisplayName("Protected endpoint with altered token returns standardised 401 JSON")
    void me_withAlteredToken_returnsJson401() throws Exception {
        User savedUser = userRepository.save(new User("altered@elibrary.ie", passwordEncoder.encode("Secure@123"), Role.USER));
        String validToken = jwtService.generateLoginResponse(savedUser).getToken();
        String alteredToken = validToken.substring(0, validToken.length() - 1)
            + (validToken.endsWith("a") ? "b" : "a");

        mockMvc.perform(get(CURRENT_USER_URL)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + alteredToken))
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.timestamp").value(notNullValue()))
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.error").value("Unauthorized"))
            .andExpect(jsonPath("$.message").value("Invalid authentication token"));
    }

    @Test
    @DisplayName("Protected endpoint with valid token returns the authenticated user profile")
    void me_withValidToken_returnsCurrentUser() throws Exception {
        User savedUser = userRepository.save(new User("reader@elibrary.ie", passwordEncoder.encode("Secure@123"), Role.USER));
        LoginResponse loginResponse = jwtService.generateLoginResponse(savedUser);

        mockMvc.perform(get(CURRENT_USER_URL)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + loginResponse.getToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(savedUser.getId()))
            .andExpect(jsonPath("$.email").value("reader@elibrary.ie"))
            .andExpect(jsonPath("$.role").value("USER"))
            .andExpect(jsonPath("$.createdAt").value(notNullValue()))
            .andExpect(jsonPath("$.password").doesNotExist());
    }

    private String buildExpiredToken(User user) {
        Instant issuedAt = Instant.now().minus(2, ChronoUnit.HOURS).truncatedTo(ChronoUnit.SECONDS);
        Instant expiresAt = issuedAt.plusSeconds(60);

        return Jwts.builder()
            .subject(user.getEmail())
            .issuer(jwtIssuer)
            .issuedAt(Date.from(issuedAt))
            .expiration(Date.from(expiresAt))
            .claim("userId", user.getId())
            .claim("email", user.getEmail())
            .claim("role", user.getRole().name())
            .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
            .compact();
    }
}
