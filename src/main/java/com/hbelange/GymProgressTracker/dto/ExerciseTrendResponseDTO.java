package com.hbelange.GymProgressTracker.dto;

import java.util.List;

public record ExerciseTrendResponseDTO (
    List<ExerciseTrendPointDTO> series,
    List<WeeklyAverageDTO> weeklyAverages
) {}
