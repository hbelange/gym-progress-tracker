package com.hbelange.GymProgressTracker.service;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;

import com.hbelange.GymProgressTracker.TestcontainersConfiguration;
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

    @Test
    @WithMockUser(username = "harrison")
    void testMeasurementTrendSeriesOnlyIncludesRequestedType() {
        LocalDate today = LocalDate.now();
        measurementService.createMeasurement(new MeasurementRequestDTO(today, MeasurementType.WEIGHT, 181.5), "harrison");
        measurementService.createMeasurement(new MeasurementRequestDTO(today, MeasurementType.STEPS, 9000.0), "harrison");

        MeasurementTrendResponseDTO trend = trendService.getMeasurementTrend("harrison", MeasurementType.WEIGHT, TrendRange.WEEK);

        assertTrue(trend.series().stream().anyMatch(point -> point.date().equals(today) && point.value().equals(181.5)));
        assertTrue(trend.series().stream().noneMatch(point -> point.value().equals(9000.0)));
    }

    @Test
    @WithMockUser(username = "harrison")
    void testMeasurementTrendSeriesRangeBoundaryIsExactlySevenDays() {
        LocalDate today = LocalDate.now();
        measurementService.createMeasurement(new MeasurementRequestDTO(today.minusDays(6), MeasurementType.WEIGHT, 200.1), "harrison");
        measurementService.createMeasurement(new MeasurementRequestDTO(today.minusDays(7), MeasurementType.WEIGHT, 300.1), "harrison");

        MeasurementTrendResponseDTO trend = trendService.getMeasurementTrend("harrison", MeasurementType.WEIGHT, TrendRange.WEEK);

        assertTrue(trend.series().stream().anyMatch(point -> point.value().equals(200.1)));
        assertTrue(trend.series().stream().noneMatch(point -> point.value().equals(300.1)));
    }

    @Test
    @WithMockUser(username = "harrison")
    void testWeeklyAveragesIncludePointsOutsideSelectedRange() {
        LocalDate today = LocalDate.now();
        measurementService.createMeasurement(new MeasurementRequestDTO(today.minusDays(15), MeasurementType.STEPS, 7777.0), "harrison");

        MeasurementTrendResponseDTO trend = trendService.getMeasurementTrend("harrison", MeasurementType.STEPS, TrendRange.WEEK);

        assertTrue(trend.series().stream().noneMatch(point -> point.value().equals(7777.0)));
        assertTrue(trend.weeklyAverages().stream().anyMatch(w -> w.average().equals(7777.0)));
    }

    @Test
    @WithMockUser(username = "harrison")
    void testWeeklyAveragesOmitEmptyBuckets() {
        LocalDate today = LocalDate.now();
        LocalDate currentWeekStart = today.minusDays(6);
        LocalDate twoWeeksAgoStart = today.minusDays(20);

        measurementService.createMeasurement(new MeasurementRequestDTO(today, MeasurementType.CALORIES, 2500.5), "harrison");
        measurementService.createMeasurement(new MeasurementRequestDTO(today.minusDays(15), MeasurementType.CALORIES, 1800.5), "harrison");

        MeasurementTrendResponseDTO trend = trendService.getMeasurementTrend("harrison", MeasurementType.CALORIES, TrendRange.WEEK);

        List<WeeklyAverageDTO> weeklyAverages = trend.weeklyAverages();
        assertTrue(weeklyAverages.stream().anyMatch(w -> w.weekStart().equals(currentWeekStart) && w.average().equals(2500.5)));
        assertTrue(weeklyAverages.stream().anyMatch(w -> w.weekStart().equals(twoWeeksAgoStart) && w.average().equals(1800.5)));
        assertTrue(weeklyAverages.stream().noneMatch(w -> w.weekStart().equals(today.minusDays(13))));
        assertTrue(weeklyAverages.stream().noneMatch(w -> w.weekStart().equals(today.minusDays(27))));
    }

    @Test
    @WithMockUser(username = "harrison")
    void testExerciseTrendUsesBestE1rmPerDay() {
        WorkoutTypeResponseDTO type = workoutTypeService.createWorkoutType(new WorkoutTypeRequestDTO("Push Day Trend Test"), "harrison");
        WorkoutResponseDTO workout = workoutService.createWorkout(new WorkoutRequestDTO(LocalDate.now(), type.id()), "harrison");
        ExerciseResponseDTO benchPress = exerciseService.createExercise(new ExerciseRequestDTO("Bench Press Trend Test"), "harrison");

        workoutService.addSetToWorkout(new SetRequestDTO(workout.id(), benchPress.id(), 5, 2, 200.0));
        workoutService.addSetToWorkout(new SetRequestDTO(workout.id(), benchPress.id(), 8, 1, 185.0));

        ExerciseTrendResponseDTO trend = trendService.getExerciseTrend("harrison", benchPress.id(), TrendRange.WEEK);

        double e1rmA = 200.0 * (1 + 5 / 30.0);
        double e1rmB = 185.0 * (1 + 8 / 30.0);
        double expectedBestE1rm = Math.max(e1rmA, e1rmB);

        assertEquals(1, trend.series().size());
        assertTrue(trend.series().stream().anyMatch(point ->
                point.date().equals(LocalDate.now()) && Math.abs(point.e1rm() - expectedBestE1rm) < 0.001));
    }

    @Test
    @WithMockUser(username = "harrison")
    void testExerciseTrendHasOnePointPerDay() {
        WorkoutTypeResponseDTO type = workoutTypeService.createWorkoutType(new WorkoutTypeRequestDTO("Push Day Trend Test 2"), "harrison");
        ExerciseResponseDTO squat = exerciseService.createExercise(new ExerciseRequestDTO("Squat Trend Test"), "harrison");

        WorkoutResponseDTO todayWorkout = workoutService.createWorkout(new WorkoutRequestDTO(LocalDate.now(), type.id()), "harrison");
        workoutService.addSetToWorkout(new SetRequestDTO(todayWorkout.id(), squat.id(), 5, 2, 225.0));

        WorkoutResponseDTO yesterdayWorkout = workoutService.createWorkout(new WorkoutRequestDTO(LocalDate.now().minusDays(1), type.id()), "harrison");
        workoutService.addSetToWorkout(new SetRequestDTO(yesterdayWorkout.id(), squat.id(), 5, 2, 215.0));

        ExerciseTrendResponseDTO trend = trendService.getExerciseTrend("harrison", squat.id(), TrendRange.WEEK);

        assertEquals(2, trend.series().size());
    }
}
