package com.hbelange.GymProgressTracker.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import com.hbelange.GymProgressTracker.dto.ExerciseActivityDTO;
import com.hbelange.GymProgressTracker.dto.ExerciseRequestDTO;
import com.hbelange.GymProgressTracker.dto.ExerciseResponseDTO;
import com.hbelange.GymProgressTracker.dto.ExerciseTrendResponseDTO;
import com.hbelange.GymProgressTracker.dto.MeasurementRequestDTO;
import com.hbelange.GymProgressTracker.dto.MeasurementTrendResponseDTO;
import com.hbelange.GymProgressTracker.dto.SetRequestDTO;
import com.hbelange.GymProgressTracker.dto.TrendRange;
import com.hbelange.GymProgressTracker.dto.WeeklyAverageDTO;
import com.hbelange.GymProgressTracker.dto.WorkoutRequestDTO;
import com.hbelange.GymProgressTracker.dto.WorkoutResponseDTO;
import com.hbelange.GymProgressTracker.dto.WorkoutTypeRequestDTO;
import com.hbelange.GymProgressTracker.dto.WorkoutTypeResponseDTO;
import com.hbelange.GymProgressTracker.entity.MeasurementType;
import com.hbelange.GymProgressTracker.entity.User;
import com.hbelange.GymProgressTracker.repository.UserRepository;
import com.hbelange.GymProgressTracker.security.SecurityUser;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class TrendServiceTest {

    @Autowired
    private TrendService trendService;

    @Autowired
    private MeasurementService measurementService;

    @Autowired
    private ExerciseService exerciseService;

    @Autowired
    private WorkoutService workoutService;

    @Autowired
    private WorkoutTypeService workoutTypeService;

    @Autowired
    private UserRepository userRepository;

    private Long userId;

    @BeforeEach
    void authenticateAsFreshUser() {
        String username = "trend-service-test-" + UUID.randomUUID();
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

    @Test
    void testMeasurementTrendSeriesOnlyIncludesRequestedType() {
        LocalDate today = LocalDate.now();
        measurementService.createMeasurement(new MeasurementRequestDTO(today, MeasurementType.WEIGHT, 181.5), userId);
        measurementService.createMeasurement(new MeasurementRequestDTO(today, MeasurementType.STEPS, 9000.0), userId);

        MeasurementTrendResponseDTO trend = trendService.getMeasurementTrend(userId, MeasurementType.WEIGHT, TrendRange.WEEK);

        assertTrue(trend.series().stream().anyMatch(point -> point.date().equals(today) && point.value().equals(181.5)));
        assertTrue(trend.series().stream().noneMatch(point -> point.value().equals(9000.0)));
    }

    @Test
    void testMeasurementTrendSeriesRangeBoundaryIsExactlySevenDays() {
        LocalDate today = LocalDate.now();
        measurementService.createMeasurement(new MeasurementRequestDTO(today.minusDays(6), MeasurementType.WEIGHT, 200.1), userId);
        measurementService.createMeasurement(new MeasurementRequestDTO(today.minusDays(7), MeasurementType.WEIGHT, 300.1), userId);

        MeasurementTrendResponseDTO trend = trendService.getMeasurementTrend(userId, MeasurementType.WEIGHT, TrendRange.WEEK);

        assertTrue(trend.series().stream().anyMatch(point -> point.value().equals(200.1)));
        assertTrue(trend.series().stream().noneMatch(point -> point.value().equals(300.1)));
    }

    @Test
    void testWeeklyAveragesIncludePointsOutsideSelectedRange() {
        LocalDate today = LocalDate.now();
        measurementService.createMeasurement(new MeasurementRequestDTO(today.minusDays(15), MeasurementType.STEPS, 7777.0), userId);

        MeasurementTrendResponseDTO trend = trendService.getMeasurementTrend(userId, MeasurementType.STEPS, TrendRange.WEEK);

        assertTrue(trend.series().stream().noneMatch(point -> point.value().equals(7777.0)));
        assertTrue(trend.weeklyAverages().stream().anyMatch(w -> w.average().equals(7777.0)));
    }

    @Test
    void testWeeklyAveragesOmitEmptyBuckets() {
        LocalDate today = LocalDate.now();
        LocalDate currentWeekStart = today.minusDays(6);
        LocalDate twoWeeksAgoStart = today.minusDays(20);

        measurementService.createMeasurement(new MeasurementRequestDTO(today, MeasurementType.CALORIES, 2500.5), userId);
        measurementService.createMeasurement(new MeasurementRequestDTO(today.minusDays(15), MeasurementType.CALORIES, 1800.5), userId);

        MeasurementTrendResponseDTO trend = trendService.getMeasurementTrend(userId, MeasurementType.CALORIES, TrendRange.WEEK);

        List<WeeklyAverageDTO> weeklyAverages = trend.weeklyAverages();
        assertTrue(weeklyAverages.stream().anyMatch(w -> w.weekStart().equals(currentWeekStart) && w.average().equals(2500.5)));
        assertTrue(weeklyAverages.stream().anyMatch(w -> w.weekStart().equals(twoWeeksAgoStart) && w.average().equals(1800.5)));
        assertTrue(weeklyAverages.stream().noneMatch(w -> w.weekStart().equals(today.minusDays(13))));
        assertTrue(weeklyAverages.stream().noneMatch(w -> w.weekStart().equals(today.minusDays(27))));
    }

    @Test
    void testExerciseTrendUsesBestE1rmPerDay() {
        WorkoutTypeResponseDTO type = workoutTypeService.createWorkoutType(new WorkoutTypeRequestDTO("Push Day Trend Test"), userId);
        WorkoutResponseDTO workout = workoutService.createWorkout(new WorkoutRequestDTO(LocalDate.now(), type.id()), userId);
        ExerciseResponseDTO benchPress = exerciseService.createExercise(new ExerciseRequestDTO("Bench Press Trend Test"), userId);

        workoutService.addSetToWorkout(new SetRequestDTO(workout.id(), benchPress.id(), 5, 2, 200.0));
        workoutService.addSetToWorkout(new SetRequestDTO(workout.id(), benchPress.id(), 8, 1, 185.0));

        ExerciseTrendResponseDTO trend = trendService.getExerciseTrend(userId, benchPress.id(), TrendRange.WEEK);

        double e1rmA = 200.0 * (1 + 5 / 30.0);
        double e1rmB = 185.0 * (1 + 8 / 30.0);
        double expectedBestE1rm = Math.max(e1rmA, e1rmB);

        assertEquals(1, trend.series().size());
        assertTrue(trend.series().stream().anyMatch(point ->
                point.date().equals(LocalDate.now()) && Math.abs(point.e1rm() - expectedBestE1rm) < 0.001));
    }

    @Test
    void testExerciseTrendHasOnePointPerDay() {
        WorkoutTypeResponseDTO type = workoutTypeService.createWorkoutType(new WorkoutTypeRequestDTO("Push Day Trend Test 2"), userId);
        ExerciseResponseDTO squat = exerciseService.createExercise(new ExerciseRequestDTO("Squat Trend Test"), userId);

        WorkoutResponseDTO todayWorkout = workoutService.createWorkout(new WorkoutRequestDTO(LocalDate.now(), type.id()), userId);
        workoutService.addSetToWorkout(new SetRequestDTO(todayWorkout.id(), squat.id(), 5, 2, 225.0));

        WorkoutResponseDTO yesterdayWorkout = workoutService.createWorkout(new WorkoutRequestDTO(LocalDate.now().minusDays(1), type.id()), userId);
        workoutService.addSetToWorkout(new SetRequestDTO(yesterdayWorkout.id(), squat.id(), 5, 2, 215.0));

        ExerciseTrendResponseDTO trend = trendService.getExerciseTrend(userId, squat.id(), TrendRange.WEEK);

        assertEquals(2, trend.series().size());
    }

    @Test
    void testExercisesOrderedByMostRecentFirst() {
        WorkoutTypeResponseDTO type = workoutTypeService.createWorkoutType(new WorkoutTypeRequestDTO("Leg Day Recent Activity Test"), userId);
        ExerciseResponseDTO squat = exerciseService.createExercise(new ExerciseRequestDTO("Squat Recent Activity Test"), userId);
        ExerciseResponseDTO legCurl = exerciseService.createExercise(new ExerciseRequestDTO("Leg Curl Recent Activity Test"), userId);

        WorkoutResponseDTO oldWorkout = workoutService.createWorkout(new WorkoutRequestDTO(LocalDate.now().minusDays(5), type.id()), userId);
        workoutService.addSetToWorkout(new SetRequestDTO(oldWorkout.id(), squat.id(), 5, 2, 225.0));

        WorkoutResponseDTO recentWorkout = workoutService.createWorkout(new WorkoutRequestDTO(LocalDate.now(), type.id()), userId);
        workoutService.addSetToWorkout(new SetRequestDTO(recentWorkout.id(), legCurl.id(), 12, 2, 60.0));

        List<ExerciseActivityDTO> exercises = trendService.getExercisesByRecentActivity(userId);

        int legCurlIndex = indexOfExercise(exercises, legCurl.id());
        int squatIndex = indexOfExercise(exercises, squat.id());
        assertTrue(legCurlIndex < squatIndex);
    }

    @Test
    void testExercisesWithoutSetsAreExcluded() {
        ExerciseResponseDTO neverLogged = exerciseService.createExercise(new ExerciseRequestDTO("Never Logged Exercise Test"), userId);

        List<ExerciseActivityDTO> exercises = trendService.getExercisesByRecentActivity(userId);

        assertTrue(exercises.stream().noneMatch(e -> e.exerciseId().equals(neverLogged.id())));
    }

    private int indexOfExercise(List<ExerciseActivityDTO> exercises, Long exerciseId) {
        for (int i = 0; i < exercises.size(); i++) {
            if (exercises.get(i).exerciseId().equals(exerciseId)) {
                return i;
            }
        }
        throw new AssertionError("Exercise not found in list: " + exerciseId);
    }
}
