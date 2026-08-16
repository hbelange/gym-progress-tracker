package com.hbelange.GymProgressTracker.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.hbelange.GymProgressTracker.TestcontainersConfiguration;
import com.hbelange.GymProgressTracker.entity.User;
import com.hbelange.GymProgressTracker.repository.UserRepository;
import com.hbelange.GymProgressTracker.security.SecurityUser;
import com.hbelange.GymProgressTracker.service.DemoAccountService;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DemoAccountService demoAccountService;

    private User findOrCreateDemoUser() {
        return userRepository.findByUsername(demoAccountService.getDemoUsername())
                .orElseGet(() -> {
                    User demoUser = new User();
                    demoUser.setUsername(demoAccountService.getDemoUsername());
                    demoUser.setEmail(demoAccountService.getDemoUsername() + "@example.com");
                    demoUser.setPassword("password");
                    demoUser.setEnabled(1);
                    demoUser.setAuthorities(List.of());
                    return userRepository.save(demoUser);
                });
    }

    @Test
    @Transactional
    void updateUsernameOnDemoAccountReturnsForbidden() throws Exception {
        User demoUser = findOrCreateDemoUser();

        mockMvc.perform(put("/api/account/username")
                .with(user(new SecurityUser(demoUser)))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"newUsername\":\"hacker\"}"))
                .andExpect(status().isForbidden());
    }

}
