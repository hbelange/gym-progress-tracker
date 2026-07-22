package com.hbelange.GymProgressTracker.service;

import java.util.List;

import com.hbelange.GymProgressTracker.dto.ExerciseTrendResponseDTO;
import com.hbelange.GymProgressTracker.dto.MeasurementTrendResponseDTO;
import com.hbelange.GymProgressTracker.dto.TrendRange;
import com.hbelange.GymProgressTracker.entity.MeasurementType;
import com.hbelange.GymProgressTracker.dto.ExerciseActivityDTO;

public interface TrendService {
    MeasurementTrendResponseDTO getMeasurementTrend(String username, MeasurementType type, TrendRange range);

    ExerciseTrendResponseDTO getExerciseTrend(String string, Long id, TrendRange week);

    List<ExerciseActivityDTO> getExercisesByRecentActivity(String username);
}
