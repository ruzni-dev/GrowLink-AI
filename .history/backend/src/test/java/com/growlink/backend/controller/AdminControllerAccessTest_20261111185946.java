package com.growlink.backend.controller;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import com.growlink.backend.entity.User;
import com.growlink.backend.entity.UserRole;
import com.growlink.backend.entity.UserStatus;
import com.growlink.backend.repository.UserRepository;

@SpringBootTest(properties = {
        "app.jwt.secret=VGhpcy1pcy1hLXRlc3Qta2V5LXRoYXQtaXMtbG9uZy1lbm91Z2gtZm9yLUhTMjU2",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.super-admin.name=",
        "app.super-admin.email=",
        "app.super-admin.password="
})
@AutoConfigureMockMvc
class AdminControllerAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void superAdminCanAccessProtectedAdminEndpoint() throws Exception {
        User admin = new User();
        admin.setFullName("System Administrator");
        admin.setEmail("admin-access@example.com");
        admin.setPassword(passwordEncoder.encode("StrongPass1"));
        admin.setLocation("System");
        admin.setRole(UserRole.SUPER_ADMIN);
        admin.setStatus(UserStatus.ACTIVE);
        userRepository.save(admin);

        String token = loginAs("admin-access@example.com", "StrongPass1");

        mockMvc.perform(get("/api/admin/health").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ok")))
                .andExpect(jsonPath("$.message", is("Super Admin access verified")));
    }

    @Test
    void regularUserCannotAccessProtectedAdminEndpoint() throws Exception {
        User user = new User();
        user.setFullName("Normal User");
        user.setEmail("regular-user@example.com");
        user.setPassword(passwordEncoder.encode("StrongPass1"));
        user.setLocation("Colombo");
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        String token = loginAs("regular-user@example.com", "StrongPass1");

        mockMvc.perform(get("/api/admin/health").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    private String loginAs(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("accessToken").asText();
    }
}
