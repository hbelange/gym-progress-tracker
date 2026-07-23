package com.hbelange.GymProgressTracker.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.hbelange.GymProgressTracker.dto.ExerciseActivityDTO;
import com.hbelange.GymProgressTracker.dto.ExerciseTrendPointDTO;
import com.hbelange.GymProgressTracker.dto.ExerciseTrendResponseDTO;
import com.hbelange.GymProgressTracker.dto.MeasurementTrendPointDTO;
import com.hbelange.GymProgressTracker.dto.MeasurementTrendResponseDTO;
import com.hbelange.GymProgressTracker.dto.TrendPoint;
import com.hbelange.GymProgressTracker.dto.TrendRange;
import com.hbelange.GymProgressTracker.dto.WeeklyAverageDTO;
import com.hbelange.GymProgressTracker.entity.Measurement;
import com.hbelange.GymProgressTracker.entity.MeasurementType;
import com.hbelange.GymProgressTracker.entity.Set;
import com.hbelange.GymProgressTracker.repository.MeasurementRepository;
import com.hbelange.GymProgressTracker.repository.SetRepository;

@Service
public class TrendServiceImpl implements TrendService {

    private final MeasurementRepository measurementRepository;
    private final SetRepository setRepository;

    public TrendServiceImpl(MeasurementRepository measurementRepository, SetRepository setRepository) {
        this.measurementRepository = measurementRepository;
        this.setRepository = setRepository;
    }

    @Override
    public MeasurementTrendResponseDTO getMeasurementTrend(String username, MeasurementType type, TrendRange range) {

        LocalDate today = LocalDate.now();

        List<MeasurementTrendPointDTO> series = toPoints(measurementRepository.findAllByUser_UsernameAndTypeAndDateBetween(
            username,
            type,
            today.minusDays(range.getDays() - 1),
            today
        ));

        // Weekly averages always cover the last 4 rolling weeks, independent of the selected range.
        List<MeasurementTrendPointDTO> lastFourWeeks = toPoints(measurementRepository.findAllByUser_UsernameAndTypeAndDateBetween(
            username,
            type,
            today.minusDays(27),
            today
        ));

        return new MeasurementTrendResponseDTO(series, computeWeeklyAverages(lastFourWeeks, today));
    }

    private List<MeasurementTrendPointDTO> toPoints(List<Measurement> measurements) {
        return measurements.stream()
            .map(measurement -> new MeasurementTrendPointDTO(measurement.getDate(), measurement.getValue()))
            .toList();
    }

    private <T extends TrendPoint> List<WeeklyAverageDTO> computeWeeklyAverages(List<T> measurements, LocalDate today) {
        
        Map<Integer, List<Double>> buckets = new HashMap<>();
        for (T measurement : measurements) {
            long daysAgo = ChronoUnit.DAYS.between(measurement.date(), today);
            int weekNumber = (int) (daysAgo / 7);
            buckets.computeIfAbsent(weekNumber, k -> new ArrayList<>()).add(measurement.value());
        }

        return buckets.entrySet().stream()
            .map(entry -> {
                int weekNumber = entry.getKey();
                List<Double> values = entry.getValue();
                double average = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                LocalDate weekStartDate = today.minusWeeks(weekNumber + 1).plusDays(1);
                return new WeeklyAverageDTO(weekStartDate, average);
            })
            .toList();

    }

    @Override
    public ExerciseTrendResponseDTO getExerciseTrend(String username, Long id, TrendRange week) {
        
        LocalDate today = LocalDate.now();

        // Get estimated one rep max for every day - if more than one set for date, take the max one rep max for that day.
        Map<LocalDate, Double> dailyOneRepMax = new HashMap<>();
        for (Set set : setRepository.findAllByUser_UsernameAndExercise_Id(username, id)) {
            double oneRepMax = set.getWeight() * (1 + (set.getReps() / 30.0));
            dailyOneRepMax.merge(set.getWorkout().getDate(), oneRepMax, Double::max);
        }

        List<ExerciseTrendPointDTO> series = dailyOneRepMax.entrySet().stream()
            .filter(entry -> !entry.getKey().isBefore(today.minusDays(week.getDays() - 1)))
            .map(entry -> new ExerciseTrendPointDTO(entry.getKey(), entry.getValue()))
            .toList();

        List<ExerciseTrendPointDTO> lastFourWeeks = dailyOneRepMax.entrySet().stream()
            .filter(entry -> !entry.getKey().isBefore(today.minusDays(27)))
            .map(entry -> new ExerciseTrendPointDTO(entry.getKey(), entry.getValue()))
            .toList();

        return new ExerciseTrendResponseDTO(series, computeWeeklyAverages(lastFourWeeks, today));
    }

    @Override
    public List<ExerciseActivityDTO> getExercisesByRecentActivity(String username) {

        List<ExerciseActivityDTO> exercises = new ArrayList<>();
        
        setRepository.findAllByUser_Username(username).forEach(set -> {
            exercises.add(new ExerciseActivityDTO(set.getExercise().getId(), set.getExercise().getName(), set.getWorkout().getDate()));
        });

        return exercises.stream()
                .collect(Collectors.groupingBy(ExerciseActivityDTO::exerciseId, Collectors.maxBy(Comparator.comparing(ExerciseActivityDTO::lastWorkoutDate))))
                .values().stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted(Comparator.comparing(ExerciseActivityDTO::lastWorkoutDate).reversed())
                .toList();    
    }
}
