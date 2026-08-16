package com.hbelange.GymProgressTracker.controller;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.hbelange.GymProgressTracker.TestcontainersConfiguration;
import com.hbelange.GymProgressTracker.dto.ExerciseRequestDTO;
import com.hbelange.GymProgressTracker.dto.ExerciseResponseDTO;
import com.hbelange.GymProgressTracker.entity.User;
import com.hbelange.GymProgressTracker.repository.UserRepository;
import com.hbelange.GymProgressTracker.security.SecurityUser;
import com.hbelange.GymProgressTracker.service.ExerciseService;
import com.hbelange.GymProgressTracker.service.TrendService;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public class TrendControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExerciseService exerciseService;

    @Autowired
    private UserRepository userRepository;

    private User createUser() {
        String username = "trend-controller-test-" + UUID.randomUUID();
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword("password");
        user.setEnabled(1);
        user.setAuthorities(List.of());
        return userRepository.save(user);
    }

    @Test
    void getMeasurementTrendUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/trend/measurements"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMeasurementTrendAuthenticated() throws Exception {
        mockMvc.perform(get("/api/trend/measurements").with(user(new SecurityUser(createUser()))))
                .andExpect(status().isOk());
    }

    @Test
    void getExercisesByRecentActivityUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/trend/exercises"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getExercisesByRecentActivityAuthenticated() throws Exception {
        mockMvc.perform(get("/api/trend/exercises").with(user(new SecurityUser(createUser()))))
                .andExpect(status().isOk());
    }

    @Test
    void getExerciseTrendUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/trend/exercises/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getExerciseTrendAuthenticatedAndOwned() throws Exception {
        User owner = createUser();
        ExerciseResponseDTO exercise = exerciseService.createExercise(new ExerciseRequestDTO("Overhead Press Controller Test"), owner.getId());

        mockMvc.perform(get("/api/trend/exercises/" + exercise.id()).with(user(new SecurityUser(owner))))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void trendRedirectsToMeasurements() throws Exception {
        mockMvc.perform(get("/trend"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/trend/measurements"));
    }

    @Test
    @WithMockUser
    void measurementTrendPageLoads() throws Exception {
        mockMvc.perform(get("/trend/measurements"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void exerciseListPageLoads() throws Exception {
        mockMvc.perform(get("/trend/exercises"))
                .andExpect(status().isOk());
    }

    @Test
    void exerciseTrendPageLoads() throws Exception {
        User owner = createUser();
        ExerciseResponseDTO exercise = exerciseService.createExercise(new ExerciseRequestDTO("Deadlift Controller Test"), owner.getId());

        mockMvc.perform(get("/trend/exercises/" + exercise.id()).with(user(new SecurityUser(owner))))
                .andExpect(status().isOk());
    }
}
