package com.hbelange.GymProgressTracker.service;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.hbelange.GymProgressTracker.TestcontainersConfiguration;
import com.hbelange.GymProgressTracker.dto.ExerciseRequestDTO;
import com.hbelange.GymProgressTracker.dto.ExerciseResponseDTO;
import com.hbelange.GymProgressTracker.entity.User;
import com.hbelange.GymProgressTracker.repository.ExerciseRepository;
import com.hbelange.GymProgressTracker.repository.UserRepository;
import com.hbelange.GymProgressTracker.security.SecurityUser;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class ExerciseServiceTest {

    @Autowired
    private ExerciseService exerciseService;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private UserRepository userRepository;

    private User createUser() {
        String username = "exercise-service-test-" + UUID.randomUUID();
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword("password");
        user.setEnabled(1);
        user.setAuthorities(List.of());
        return userRepository.save(user);
    }

    private void authenticateAs(User user) {
        SecurityUser securityUser = new SecurityUser(user);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities()));
        SecurityContextHolder.setContext(context);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testUpdateExerciseWithNoUser() {
        assertThrows(IllegalArgumentException.class, () -> {
            exerciseService.updateExercise(1L, null);
        });

    }

    @Test
    void testUpdateExerciseWithUserButWrongUser() {
        User owner = createUser();
        ExerciseResponseDTO created = exerciseService.createExercise(new ExerciseRequestDTO("Leg Press"), owner.getId());

        authenticateAs(createUser());

        assertThrows(AuthorizationDeniedException.class, () -> {
            exerciseService.updateExercise(created.id(), new ExerciseRequestDTO("Barbell Squat"));
        });
    }

    @Test
    void testUpdateExerciseWithUserAndCorrectUser() {
        User owner = createUser();
        authenticateAs(owner);
        ExerciseResponseDTO created = exerciseService.createExercise(new ExerciseRequestDTO("Hack Squat"), owner.getId());

        ExerciseResponseDTO result = assertDoesNotThrow(
            () -> exerciseService.updateExercise(created.id(), new ExerciseRequestDTO("Barbell Squat")));

        assertEquals("Barbell Squat", result.name());
    }

    @Test
    void testDeleteExerciseWithWrongUser() {
        User owner = createUser();
        ExerciseResponseDTO created = exerciseService.createExercise(new ExerciseRequestDTO("Lat Pulldown"), owner.getId());

        authenticateAs(createUser());

        assertThrows(AuthorizationDeniedException.class, () -> {
            exerciseService.deleteExercise(created.id());
        });
        assertTrue(exerciseRepository.existsById(created.id()));
    }

    @Test
    void testDeleteExerciseWithCorrectUser() {
        User owner = createUser();
        authenticateAs(owner);
        ExerciseResponseDTO created = exerciseService.createExercise(new ExerciseRequestDTO("Cable Row"), owner.getId());

        assertDoesNotThrow(() -> exerciseService.deleteExercise(created.id()));
        assertFalse(exerciseRepository.existsById(created.id()));
    }

    @Test
    void testGetAllExercisesOnlyReturnsOwnExercises() {
        User userA = createUser();
        User userB = createUser();
        exerciseService.createExercise(new ExerciseRequestDTO("Testuser Only Exercise"), userA.getId());
        exerciseService.createExercise(new ExerciseRequestDTO("Harrison Only Exercise"), userB.getId());

        List<ExerciseResponseDTO> result = exerciseService.getAllExercises(userA.getId());

        assertTrue(result.stream().anyMatch(e -> e.name().equals("Testuser Only Exercise")));
        assertTrue(result.stream().noneMatch(e -> e.name().equals("Harrison Only Exercise")));
    }

}
