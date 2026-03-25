package com.elibrary.user_service.controller;

import com.elibrary.user_service.model.Role;
import com.elibrary.user_service.model.User;
import com.elibrary.user_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GatewayIdentityIntegrationTest {

    private static final String CURRENT_USER_URL = "/api/users/me";
    private static final String AUTHENTICATED_USER_HEADER = "X-Authenticated-User";
    private static final String AUTHENTICATED_USER_ID_HEADER = "X-Authenticated-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Protected endpoint without forwarded identity returns standardised 401 JSON")
    void me_withoutForwardedIdentity_returnsJson401() throws Exception {
        mockMvc.perform(get(CURRENT_USER_URL))
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.timestamp").value(notNullValue()))
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.error").value("Unauthorized"))
            .andExpect(jsonPath("$.message").value("Authentication token is required"));
    }

    @Test
    @DisplayName("Protected endpoint with forwarded identity returns the authenticated user profile")
    void me_withForwardedIdentity_returnsCurrentUser() throws Exception {
        User savedUser = userRepository.save(new User("reader@elibrary.ie", passwordEncoder.encode("Secure@123"), Role.USER));

        mockMvc.perform(get(CURRENT_USER_URL)
                .header(AUTHENTICATED_USER_HEADER, "reader@elibrary.ie")
                .header(AUTHENTICATED_USER_ID_HEADER, savedUser.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(savedUser.getId()))
            .andExpect(jsonPath("$.email").value("reader@elibrary.ie"))
            .andExpect(jsonPath("$.role").value("USER"))
            .andExpect(jsonPath("$.createdAt").value(notNullValue()))
            .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("Protected endpoint returns 404 when authenticated user id does not exist")
    void me_withMissingUserId_returnsJson404() throws Exception {
        userRepository.save(new User("reader@elibrary.ie", passwordEncoder.encode("Secure@123"), Role.USER));

        mockMvc.perform(get(CURRENT_USER_URL)
                .header(AUTHENTICATED_USER_HEADER, "reader@elibrary.ie")
                .header(AUTHENTICATED_USER_ID_HEADER, 999999))
            .andExpect(status().isNotFound())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.timestamp").value(notNullValue()))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    @DisplayName("Protected endpoint with unknown forwarded identity returns standardised 401 JSON")
    void me_withUnknownForwardedIdentity_returnsJson401() throws Exception {
        mockMvc.perform(get(CURRENT_USER_URL)
                .header(AUTHENTICATED_USER_HEADER, "missing@elibrary.ie"))
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.timestamp").value(notNullValue()))
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.error").value("Unauthorized"))
            .andExpect(jsonPath("$.message").value("Invalid authentication token"));
    }

    @Test
    @DisplayName("Inactive accounts remain blocked even with forwarded identity")
    void me_withInactiveForwardedIdentity_returnsJson401() throws Exception {
        userRepository.save(new User("inactive@elibrary.ie", passwordEncoder.encode("Secure@123"), Role.USER, false));

        mockMvc.perform(get(CURRENT_USER_URL)
                .header(AUTHENTICATED_USER_HEADER, "inactive@elibrary.ie"))
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.timestamp").value(notNullValue()))
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.error").value("Unauthorized"))
            .andExpect(jsonPath("$.message").value("Invalid authentication token"));
    }
}
