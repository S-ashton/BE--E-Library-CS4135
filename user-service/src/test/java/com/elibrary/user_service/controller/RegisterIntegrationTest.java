package com.elibrary.user_service.controller;

import com.elibrary.user_service.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Integration tests for POST /api/auth/register.
// Covers all acceptance criteria of US-USER-01 (FR-USER-01.1 – FR-USER-01.6).
// Uses an in-memory H2 database, cleared before each test.
@SpringBootTest
@AutoConfigureMockMvc
class RegisterIntegrationTest {

    private static final String REGISTER_URL = "/api/auth/register";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("FR-USER-01 — Successful registration returns 201 with safe response")
    void register_validRequest_returns201WithNoPassword() throws Exception {
        var payload = Map.of(
                "email", "alice@elibrary.ie",
                "password", "Secure@123",
                "role", "USER"
        );

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("alice@elibrary.ie"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("FR-USER-01.4 — role field in body is ignored; account always created as USER")
    void register_staffRole_returns201() throws Exception {
        var payload = Map.of(
                "email", "staff@elibrary.ie",
                "password", "Secure@123",
                "role", "STAFF"
        );

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("FR-USER-01.4 — role field in body is ignored; account always created as USER")
    void register_adminRole_returns201() throws Exception {
        var payload = Map.of(
                "email", "admin@elibrary.ie",
                "password", "Secure@123",
                "role", "ADMIN"
        );

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("FR-USER-01.2 — Duplicate email returns 409 Conflict")
    void register_duplicateEmail_returns409() throws Exception {
        var payload = Map.of(
                "email", "duplicate@elibrary.ie",
                "password", "Secure@123",
                "role", "USER"
        );

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated());

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(containsString("duplicate@elibrary.ie")));
    }

    @Test
    @DisplayName("FR-USER-01.1 — Missing email returns 400")
    void register_missingEmail_returns400() throws Exception {
        var payload = Map.of(
                "password", "Secure@123",
                "role", "USER"
        );

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field == 'email')]").exists());
    }

    @Test
    @DisplayName("FR-USER-01.1 — Malformed email returns 400")
    void register_invalidEmailFormat_returns400() throws Exception {
        var payload = Map.of(
                "email", "not-an-email",
                "password", "Secure@123",
                "role", "USER"
        );

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field == 'email')].message",
                        hasItem(containsString("valid format"))));
    }

    @Test
    @DisplayName("FR-USER-01.3 — Password too short returns 400")
    void register_shortPassword_returns400() throws Exception {
        var payload = Map.of(
                "email", "user@elibrary.ie",
                "password", "Ab1@",
                "role", "USER"
        );

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field == 'password')]").exists());
    }

    @Test
    @DisplayName("FR-USER-01.3 — @Size message shown when password is under 8 characters")
    void register_shortPassword_sizeMessageReturned() throws Exception {
        var payload = Map.of(
                "email", "user2@elibrary.ie",
                "password", "Ab1@",
                "role", "USER"
        );

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(
                        "$.errors[?(@.field == 'password')].message",
                        hasItem(containsString("at least 8 characters"))));
    }

    @Test
    @DisplayName("FR-USER-01.3 — Password missing uppercase returns 400")
    void register_noUppercasePassword_returns400() throws Exception {
        var payload = Map.of(
                "email", "user@elibrary.ie",
                "password", "secure@123",
                "role", "USER"
        );

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field == 'password')]").exists());
    }

    @Test
    @DisplayName("FR-USER-01.3 — Password missing special character returns 400")
    void register_noSpecialCharPassword_returns400() throws Exception {
        var payload = Map.of(
                "email", "user@elibrary.ie",
                "password", "Secure123",
                "role", "USER"
        );

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field == 'password')]").exists());
    }

    @Test
    @DisplayName("FR-USER-01.3 — Blank password returns 400")
    void register_blankPassword_returns400() throws Exception {
        var payload = Map.of(
                "email", "user@elibrary.ie",
                "password", "",
                "role", "USER"
        );

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field == 'password')]").exists());
    }

    @Test
    @DisplayName("FR-USER-01.4 — Omitted role defaults to USER")
    void register_missingRole_defaultsToUser() throws Exception {
        var payload = Map.of(
                "email", "user@elibrary.ie",
                "password", "Secure@123"
                // role intentionally absent — should default to USER
        );

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("FR-USER-01.6 — Multiple invalid fields returns structured errors for all fields")
    void register_multipleInvalidFields_returnsAllErrors() throws Exception {
        var payload = Map.of(
                "email", "bad-email",
                "password", "weak",
                "role", "USER"
        );

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[?(@.field == 'email')]").exists())
                .andExpect(jsonPath("$.errors[?(@.field == 'password')]").exists());
    }

    @Test
    @DisplayName("FR-USER-01.5 — Stored password is BCrypt hashed, not plaintext")
    void register_passwordStoredAsHash() throws Exception {
        var payload = Map.of(
                "email", "hash-check@elibrary.ie",
                "password", "Secure@123",
                "role", "USER"
        );

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated());

        String storedPassword = userRepository.findByEmail("hash-check@elibrary.ie")
                .orElseThrow()
                .getPassword();

        assert storedPassword.startsWith("$2") : "Password should be BCrypt hashed";
        assert !storedPassword.equals("Secure@123") : "Password must not be stored in plaintext";
    }
}
