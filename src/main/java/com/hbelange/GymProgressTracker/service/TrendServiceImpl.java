package com.hbelange.GymProgressTracker.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.hbelange.GymProgressTracker.dto.MeasurementTrendPointDTO;
import com.hbelange.GymProgressTracker.dto.MeasurementTrendResponseDTO;
import com.hbelange.GymProgressTracker.dto.TrendRange;
import com.hbelange.GymProgressTracker.dto.WeeklyAverageDTO;
import com.hbelange.GymProgressTracker.entity.MeasurementType;
import com.hbelange.GymProgressTracker.repository.MeasurementRepository;

@Service
public class TrendServiceImpl implements TrendService {

    private final MeasurementRepository measurementRepository;

    public TrendServiceImpl(MeasurementRepository measurementRepository) {
        this.measurementRepository = measurementRepository;
    }

    @Override
    public MeasurementTrendResponseDTO getMeasurementTrend(String username, MeasurementType type, TrendRange range) {
        
        LocalDate today = LocalDate.now();

        List<MeasurementTrendPointDTO> series = measurementRepository.findAllByUser_UsernameAndTypeAndDateBetween(
            username, 
            type, 
            today.minusDays(range.getDays()), 
            today
        ).stream()
         .map(measurement -> new MeasurementTrendPointDTO(measurement.getDate(), measurement.getValue()))
         .toList();

        return new MeasurementTrendResponseDTO(series, computeWeeklyAverages(series, today));
    }

    private List<WeeklyAverageDTO> computeWeeklyAverages(List<MeasurementTrendPointDTO> measurements, LocalDate today) {
        
        Map<Integer, List<Double>> buckets = new HashMap<>();
        for (MeasurementTrendPointDTO measurement : measurements) {
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
}
