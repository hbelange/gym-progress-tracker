package com.hbelange.GymProgressTracker.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hbelange.GymProgressTracker.TestcontainersConfiguration;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public class ExerciseControllerTest {
    
    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAllExercisesUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/exercise"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void getAllExercisesAuthenticated() throws Exception {
        mockMvc.perform(get("/api/exercise"))
                .andExpect(status().isOk());
    }
}
