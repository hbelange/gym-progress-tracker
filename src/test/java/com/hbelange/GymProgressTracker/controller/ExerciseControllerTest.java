package com.hbelange.GymProgressTracker.controller;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.hbelange.GymProgressTracker.TestcontainersConfiguration;
import com.hbelange.GymProgressTracker.entity.User;
import com.hbelange.GymProgressTracker.repository.UserRepository;
import com.hbelange.GymProgressTracker.security.SecurityUser;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public class ExerciseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    private User createUser() {
        String username = "exercise-controller-test-" + UUID.randomUUID();
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword("password");
        user.setEnabled(1);
        user.setAuthorities(List.of());
        return userRepository.save(user);
    }

    @Test
    void getAllExercisesUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/exercise"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAllExercisesAuthenticated() throws Exception {
        mockMvc.perform(get("/api/exercise").with(user(new SecurityUser(createUser()))))
                .andExpect(status().isOk());
    }
}
