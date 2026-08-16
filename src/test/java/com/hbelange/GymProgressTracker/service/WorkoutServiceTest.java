package com.hbelange.GymProgressTracker.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.hbelange.GymProgressTracker.TestcontainersConfiguration;
import com.hbelange.GymProgressTracker.dto.ExerciseRequestDTO;
import com.hbelange.GymProgressTracker.dto.ExerciseResponseDTO;
import com.hbelange.GymProgressTracker.dto.SetRequestDTO;
import com.hbelange.GymProgressTracker.dto.SetResponseDTO;
import com.hbelange.GymProgressTracker.dto.WorkoutRequestDTO;
import com.hbelange.GymProgressTracker.dto.WorkoutResponseDTO;
import com.hbelange.GymProgressTracker.dto.WorkoutTypeRequestDTO;
import com.hbelange.GymProgressTracker.dto.WorkoutTypeResponseDTO;
import com.hbelange.GymProgressTracker.entity.User;
import com.hbelange.GymProgressTracker.repository.SetRepository;
import com.hbelange.GymProgressTracker.repository.UserRepository;
import com.hbelange.GymProgressTracker.security.SecurityUser;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class WorkoutServiceTest {

    @Autowired
    private WorkoutService workoutService;

    @Autowired
    private WorkoutTypeService workoutTypeService;

    @Autowired
    private ExerciseService exerciseService;

    @Autowired
    private SetRepository setRepository;

    @Autowired
    private UserRepository userRepository;

    private Long userId;

    @BeforeEach
    void authenticateAsFreshUser() {
        String username = "workout-service-test-" + UUID.randomUUID();
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword("password");
        user.setEnabled(1);
        user.setAuthorities(List.of());
        user = userRepository.save(user);
        userId = user.getId();

        SecurityUser securityUser = new SecurityUser(user);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities()));
        SecurityContextHolder.setContext(context);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private WorkoutResponseDTO createWorkout(String workoutTypeName) {
        WorkoutTypeResponseDTO type = workoutTypeService.createWorkoutType(new WorkoutTypeRequestDTO(workoutTypeName), userId);
        return workoutService.createWorkout(new WorkoutRequestDTO(LocalDate.now(), type.id()), userId);
    }

    @Test
    void testAddSetToWorkoutIncludesExerciseName() {
        WorkoutResponseDTO workout = createWorkout("Push Day Exercise Name Test");
        ExerciseResponseDTO exercise = exerciseService.createExercise(new ExerciseRequestDTO("Bench Press Exercise Name Test"), userId);

        SetResponseDTO set = workoutService.addSetToWorkout(new SetRequestDTO(workout.id(), exercise.id(), 8, 2, 100.0));

        assertEquals("Bench Press Exercise Name Test", set.exerciseName());
    }

    @Test
    void testSetNumberIncrementsPerWorkoutAndExercise() {
        WorkoutResponseDTO workout = createWorkout("Push Day Set Number Test");
        ExerciseResponseDTO bench = exerciseService.createExercise(new ExerciseRequestDTO("Bench Press Set Number Test"), userId);
        ExerciseResponseDTO squat = exerciseService.createExercise(new ExerciseRequestDTO("Squat Set Number Test"), userId);

        SetResponseDTO firstBenchSet = workoutService.addSetToWorkout(new SetRequestDTO(workout.id(), bench.id(), 8, 2, 100.0));
        SetResponseDTO secondBenchSet = workoutService.addSetToWorkout(new SetRequestDTO(workout.id(), bench.id(), 6, 1, 105.0));
        SetResponseDTO firstSquatSet = workoutService.addSetToWorkout(new SetRequestDTO(workout.id(), squat.id(), 5, 3, 140.0));

        assertEquals(1, firstBenchSet.setNumber());
        assertEquals(2, secondBenchSet.setNumber());
        assertEquals(1, firstSquatSet.setNumber());
    }

    @Test
    void testDeletingSetRenumbersRemainingSetsForSameExercise() {
        WorkoutResponseDTO workout = createWorkout("Leg Day Delete Renumber Test");
        ExerciseResponseDTO legCurl = exerciseService.createExercise(new ExerciseRequestDTO("Leg Curl Delete Renumber Test"), userId);

        SetResponseDTO firstSet = workoutService.addSetToWorkout(new SetRequestDTO(workout.id(), legCurl.id(), 12, 2, 40.0));
        SetResponseDTO secondSet = workoutService.addSetToWorkout(new SetRequestDTO(workout.id(), legCurl.id(), 10, 1, 45.0));

        workoutService.deleteSet(firstSet.id());

        SetResponseDTO remaining = workoutService.getSetById(secondSet.id());
        assertEquals(1, remaining.setNumber());
    }

    @Test
    void testDeletingWorkoutDeletesItsSets() {
        WorkoutResponseDTO workout = createWorkout("Push Day Cascade Delete Test");
        ExerciseResponseDTO bench = exerciseService.createExercise(new ExerciseRequestDTO("Bench Press Cascade Delete Test"), userId);
        SetResponseDTO set = workoutService.addSetToWorkout(new SetRequestDTO(workout.id(), bench.id(), 8, 2, 100.0));

        workoutService.deleteWorkout(workout.id());

        assertFalse(setRepository.existsById(set.id()));
    }

}
