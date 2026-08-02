package com.hbelange.GymProgressTracker.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.hbelange.GymProgressTracker.TestcontainersConfiguration;
import com.hbelange.GymProgressTracker.dto.UserRequestDTO;
import com.hbelange.GymProgressTracker.entity.User;
import com.hbelange.GymProgressTracker.repository.UserRepository;
import com.hbelange.GymProgressTracker.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public class LoginTest {

    private static final String PASSWORD = "password123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private String username;

    @BeforeEach
    void setUp() {
        username = "login-" + UUID.randomUUID();
        userService.registerUser(new UserRequestDTO(username + "@example.com", username, PASSWORD));

        User user = userRepository.findByUsername(username).orElseThrow();
        user.setEnabled(1);
        userRepository.save(user);
    }

    @Test
    void loginPageIsPubliclyAccessible() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    @Test
    void unauthenticatedRequestToHomeRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void loginWithValidCredentialsSucceeds() throws Exception {
        mockMvc.perform(formLogin().user(username).password(PASSWORD))
                .andExpect(authenticated().withUsername(username))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void loginWithWrongPasswordFails() throws Exception {
        mockMvc.perform(formLogin().user(username).password("wrongPassword"))
                .andExpect(unauthenticated())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void loginWithUnknownUsernameFails() throws Exception {
        mockMvc.perform(formLogin().user("no-such-user-" + UUID.randomUUID()).password(PASSWORD))
                .andExpect(unauthenticated())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void loginWithUnverifiedAccountFails() throws Exception {
        String unverifiedUsername = "unverified-" + UUID.randomUUID();
        userService.registerUser(new UserRequestDTO(unverifiedUsername + "@example.com", unverifiedUsername, PASSWORD));

        mockMvc.perform(formLogin().user(unverifiedUsername).password(PASSWORD))
                .andExpect(unauthenticated())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?unverified&username=" + unverifiedUsername));
    }

}
