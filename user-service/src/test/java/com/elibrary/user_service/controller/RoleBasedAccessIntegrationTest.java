package com.elibrary.user_service.controller;

import com.elibrary.user_service.dto.LoginResponse;
import com.elibrary.user_service.model.Role;
import com.elibrary.user_service.model.User;
import com.elibrary.user_service.repository.UserRepository;
import com.elibrary.user_service.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RoleBasedAccessIntegrationTest {

    private static final String CURRENT_USER_URL = "/api/users/me";
    private static final String USERS_URL = "/api/users";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("USER role can access user-level profile endpoint")
    void me_withUserRole_returnsCurrentUser() throws Exception {
        User user = createUser("user@elibrary.ie", Role.USER);

        mockMvc.perform(get(CURRENT_USER_URL)
                .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(user)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("user@elibrary.ie"))
            .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("STAFF role can access user-level profile endpoint")
    void me_withStaffRole_returnsCurrentUser() throws Exception {
        User user = createUser("staff@elibrary.ie", Role.STAFF);

        mockMvc.perform(get(CURRENT_USER_URL)
                .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(user)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("staff@elibrary.ie"))
            .andExpect(jsonPath("$.role").value("STAFF"));
    }

    @Test
    @DisplayName("USER role is forbidden from admin-only endpoints")
    void users_withUserRole_returnsForbiddenJson() throws Exception {
        User user = createUser("user@elibrary.ie", Role.USER);

        mockMvc.perform(get(USERS_URL)
                .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(user)))
            .andExpect(status().isForbidden())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.timestamp").value(notNullValue()))
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.error").value("Forbidden"))
            .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    @DisplayName("STAFF role is forbidden from admin-only endpoints")
    void users_withStaffRole_returnsForbiddenJson() throws Exception {
        User user = createUser("staff@elibrary.ie", Role.STAFF);

        mockMvc.perform(get(USERS_URL)
                .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(user)))
            .andExpect(status().isForbidden())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.timestamp").value(notNullValue()))
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.error").value("Forbidden"))
            .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    @DisplayName("ADMIN role can access admin-only endpoints")
    void users_withAdminRole_returnsAllUsers() throws Exception {
        User admin = createUser("admin@elibrary.ie", Role.ADMIN);
        createUser("staff@elibrary.ie", Role.STAFF);
        createUser("user@elibrary.ie", Role.USER);

        mockMvc.perform(get(USERS_URL)
                .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(admin)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[*].email", containsInAnyOrder(
                "admin@elibrary.ie",
                "staff@elibrary.ie",
                "user@elibrary.ie"
            )));
    }

    private User createUser(String email, Role role) {
        return userRepository.save(new User(email, passwordEncoder.encode("Secure@123"), role));
    }

    private String bearerTokenFor(User user) {
        LoginResponse loginResponse = jwtService.generateLoginResponse(user);
        return "Bearer " + loginResponse.getToken();
    }
}
