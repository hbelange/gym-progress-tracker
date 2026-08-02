package com.hbelange.GymProgressTracker.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.hbelange.GymProgressTracker.TestcontainersConfiguration;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private String username;
    private String email;

    @BeforeEach
    void setUp() {
        username = "reg-ctrl-" + UUID.randomUUID();
        email = username + "@example.com";
    }

    private String requestBody(String username, String password) {
        return "{\"email\":\"" + username + "@example.com\",\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
    }

    @Test
    void registerUserWithoutCsrfReturnsForbidden() throws Exception {
        mockMvc.perform(post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody(username, "password")))
                .andExpect(status().isForbidden());
    }

    @Test
    void registerUserReturnsCreatedUser() throws Exception {
        mockMvc.perform(post("/api/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody(username, "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void registerUserWithDuplicateUsernameReturnsConflict() throws Exception {
        String body = requestBody(username, "password");

        mockMvc.perform(post("/api/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void registerUserWithBlankUsernameReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody("", "password")))
                .andExpect(status().isBadRequest());
    }

}
