package com.hbelange.GymProgressTracker.service;

import com.hbelange.GymProgressTracker.dto.ExerciseTrendResponseDTO;
import com.hbelange.GymProgressTracker.dto.MeasurementTrendResponseDTO;
import com.hbelange.GymProgressTracker.dto.TrendRange;
import com.hbelange.GymProgressTracker.entity.MeasurementType;

public interface TrendService {
    MeasurementTrendResponseDTO getMeasurementTrend(String username, MeasurementType type, TrendRange range);

    ExerciseTrendResponseDTO getExerciseTrend(String string, Long id, TrendRange week);
}
