package com.hbelange.GymProgressTracker.service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.test.context.support.WithMockUser;

import com.hbelange.GymProgressTracker.TestcontainersConfiguration;
import com.hbelange.GymProgressTracker.dto.ExerciseRequestDTO;
import com.hbelange.GymProgressTracker.dto.ExerciseResponseDTO;
import com.hbelange.GymProgressTracker.repository.ExerciseRepository;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class ExerciseServiceTest {

    @Autowired
    private ExerciseService exerciseService;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Test
    void testUpdateExerciseWithNoUser() {
        assertThrows(IllegalArgumentException.class, () -> {
            exerciseService.updateExercise(1L, null);
        });
    
    }

    @Test
    @WithMockUser(username = "testuser")
    void testUpdateExerciseWithUserButWrongUser() {
        ExerciseResponseDTO created = exerciseService.createExercise(new ExerciseRequestDTO("Leg Press"), "harrison");

        assertThrows(AuthorizationDeniedException.class, () -> {
            exerciseService.updateExercise(created.id(), new ExerciseRequestDTO("Barbell Squat"));
        });
    }

    @Test
    @WithMockUser(username = "harrison")
    void testUpdateExerciseWithUserAndCorrectUser() {
        ExerciseResponseDTO created = exerciseService.createExercise(new ExerciseRequestDTO("Hack Squat"), "harrison");

        ExerciseResponseDTO result = assertDoesNotThrow(
            () -> exerciseService.updateExercise(created.id(), new ExerciseRequestDTO("Barbell Squat")));

        assertEquals("Barbell Squat", result.name());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testDeleteExerciseWithWrongUser() {
        ExerciseResponseDTO created = exerciseService.createExercise(new ExerciseRequestDTO("Lat Pulldown"), "harrison");

        assertThrows(AuthorizationDeniedException.class, () -> {
            exerciseService.deleteExercise(created.id());
        });
        assertTrue(exerciseRepository.existsById(created.id()));
    }

    @Test
    @WithMockUser(username = "harrison")
    void testDeleteExerciseWithCorrectUser() {
        ExerciseResponseDTO created = exerciseService.createExercise(new ExerciseRequestDTO("Cable Row"), "harrison");

        assertDoesNotThrow(() -> exerciseService.deleteExercise(created.id()));
        assertFalse(exerciseRepository.existsById(created.id()));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetAllExercisesOnlyReturnsOwnExercises() {
        exerciseService.createExercise(new ExerciseRequestDTO("Testuser Only Exercise"), "testuser");
        exerciseService.createExercise(new ExerciseRequestDTO("Harrison Only Exercise"), "harrison");

        List<ExerciseResponseDTO> result = exerciseService.getAllExercises("testuser");

        assertTrue(result.stream().anyMatch(e -> e.name().equals("Testuser Only Exercise")));
        assertTrue(result.stream().noneMatch(e -> e.name().equals("Harrison Only Exercise")));
    }

}
