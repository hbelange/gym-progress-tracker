package com.hbelange.GymProgressTracker.dto;

import java.util.List;

public record MeasurementTrendResponseDTO(
    List<MeasurementTrendPointDTO> series,
    List<WeeklyAverageDTO> weeklyAverages
) {}
