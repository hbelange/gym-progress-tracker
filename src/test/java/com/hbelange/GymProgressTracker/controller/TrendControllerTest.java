package com.hbelange.GymProgressTracker.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;

import com.hbelange.GymProgressTracker.TestcontainersConfiguration;
import com.hbelange.GymProgressTracker.dto.ExerciseRequestDTO;
import com.hbelange.GymProgressTracker.dto.ExerciseResponseDTO;
import com.hbelange.GymProgressTracker.service.ExerciseService;
import com.hbelange.GymProgressTracker.service.TrendService;

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

    @Test
    void getMeasurementTrendUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/trend/measurements"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void getMeasurementTrendAuthenticated() throws Exception {
        mockMvc.perform(get("/api/trend/measurements"))
                .andExpect(status().isOk());
    }

    @Test
    void getExercisesByRecentActivityUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/trend/exercises"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void getExercisesByRecentActivityAuthenticated() throws Exception {
        mockMvc.perform(get("/api/trend/exercises"))
                .andExpect(status().isOk());
    }

    @Test
    void getExerciseTrendUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/trend/exercises/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "harrison")
    void getExerciseTrendAuthenticatedAndOwned() throws Exception {
        ExerciseResponseDTO exercise = exerciseService.createExercise(new ExerciseRequestDTO("Overhead Press Controller Test"), "harrison");

        mockMvc.perform(get("/api/trend/exercises/" + exercise.id()))
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
    @WithMockUser(username = "harrison")
    void exerciseTrendPageLoads() throws Exception {
        ExerciseResponseDTO exercise = exerciseService.createExercise(new ExerciseRequestDTO("Deadlift Controller Test"), "harrison");

        mockMvc.perform(get("/trend/exercises/" + exercise.id()))
                .andExpect(status().isOk());
    }
}
