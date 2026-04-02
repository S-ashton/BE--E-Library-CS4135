package com.elibrary.user_service.controller;

import com.elibrary.user_service.model.Role;
import com.elibrary.user_service.model.User;
import com.elibrary.user_service.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UpdateProfileIntegrationTest {

    private static final String CURRENT_USER_URL = "/api/users/me";
    private static final String AUTHENTICATED_USER_HEADER = "X-Authenticated-User";
    private static final String AUTHENTICATED_USER_ID_HEADER = "X-Authenticated-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Authenticated user can update permitted profile fields and receive the updated DTO")
    void updateProfile_withValidEmail_returnsUpdatedProfile() throws Exception {
        User user = userRepository.save(new User("reader@elibrary.ie", passwordEncoder.encode("Secure@123"), Role.USER));

        mockMvc.perform(put(CURRENT_USER_URL)
                .header(AUTHENTICATED_USER_HEADER, "reader@elibrary.ie")
                .header(AUTHENTICATED_USER_ID_HEADER, user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("email", "updated@elibrary.ie"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(user.getId()))
            .andExpect(jsonPath("$.email").value("updated@elibrary.ie"))
            .andExpect(jsonPath("$.role").value("USER"))
            .andExpect(jsonPath("$.createdAt").value(notNullValue()))
            .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("Only authenticated users can update profile data")
    void updateProfile_withoutForwardedIdentity_returnsJson401() throws Exception {
        mockMvc.perform(put(CURRENT_USER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("email", "updated@elibrary.ie"))))
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.error").value("Unauthorized"))
            .andExpect(jsonPath("$.message").value("Authentication token is required"));
    }

    @Test
    @DisplayName("Duplicate email updates are rejected with a clear conflict response")
    void updateProfile_withDuplicateEmail_returnsJson409() throws Exception {
        User user = userRepository.save(new User("reader@elibrary.ie", passwordEncoder.encode("Secure@123"), Role.USER));
        userRepository.save(new User("existing@elibrary.ie", passwordEncoder.encode("Secure@123"), Role.STAFF));

        mockMvc.perform(put(CURRENT_USER_URL)
                .header(AUTHENTICATED_USER_HEADER, "reader@elibrary.ie")
                .header(AUTHENTICATED_USER_ID_HEADER, user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("email", "existing@elibrary.ie"))))
            .andExpect(status().isConflict())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    @DisplayName("Invalid profile updates return clear validation errors")
    void updateProfile_withInvalidEmail_returnsJson400() throws Exception {
        User user = userRepository.save(new User("reader@elibrary.ie", passwordEncoder.encode("Secure@123"), Role.USER));

        mockMvc.perform(put(CURRENT_USER_URL)
                .header(AUTHENTICATED_USER_HEADER, "reader@elibrary.ie")
                .header(AUTHENTICATED_USER_ID_HEADER, user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("email", "not-an-email"))))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.errors[*].field", hasItem("email")));
    }

    @Test
    @DisplayName("Missing authenticated users receive a clear not-found error on update")
    void updateProfile_withMissingUserId_returnsJson404() throws Exception {
        userRepository.save(new User("reader@elibrary.ie", passwordEncoder.encode("Secure@123"), Role.USER));

        mockMvc.perform(put(CURRENT_USER_URL)
                .header(AUTHENTICATED_USER_HEADER, "reader@elibrary.ie")
                .header(AUTHENTICATED_USER_ID_HEADER, 999999)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("email", "updated@elibrary.ie"))))
            .andExpect(status().isNotFound())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message").value("User not found"));
    }
}
