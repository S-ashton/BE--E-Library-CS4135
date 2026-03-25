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
    @DisplayName("USER role can access user-level profile endpoint")
    void me_withUserRole_returnsCurrentUser() throws Exception {
        User user = createUser("user@elibrary.ie", Role.USER);

        mockMvc.perform(get(CURRENT_USER_URL)
                .header(AUTHENTICATED_USER_HEADER, "user@elibrary.ie")
                .header(AUTHENTICATED_USER_ID_HEADER, user.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("user@elibrary.ie"))
            .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("STAFF role can access user-level profile endpoint")
    void me_withStaffRole_returnsCurrentUser() throws Exception {
        User user = createUser("staff@elibrary.ie", Role.STAFF);

        mockMvc.perform(get(CURRENT_USER_URL)
                .header(AUTHENTICATED_USER_HEADER, "staff@elibrary.ie")
                .header(AUTHENTICATED_USER_ID_HEADER, user.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("staff@elibrary.ie"))
            .andExpect(jsonPath("$.role").value("STAFF"));
    }

    @Test
    @DisplayName("USER role is forbidden from admin-only endpoints")
    void users_withUserRole_returnsForbiddenJson() throws Exception {
        createUser("user@elibrary.ie", Role.USER);

        mockMvc.perform(get(USERS_URL)
                .header(AUTHENTICATED_USER_HEADER, "user@elibrary.ie"))
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
        createUser("staff@elibrary.ie", Role.STAFF);

        mockMvc.perform(get(USERS_URL)
                .header(AUTHENTICATED_USER_HEADER, "staff@elibrary.ie"))
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
        createUser("admin@elibrary.ie", Role.ADMIN);
        createUser("staff@elibrary.ie", Role.STAFF);
        createUser("user@elibrary.ie", Role.USER);

        mockMvc.perform(get(USERS_URL)
                .header(AUTHENTICATED_USER_HEADER, "admin@elibrary.ie"))
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
}
