package com.hbelange.GymProgressTracker.dto;

import java.time.LocalDate;

import com.hbelange.GymProgressTracker.entity.MeasurementType;

public record MeasurementResponseDTO(
    Long id,
    LocalDate date,
    MeasurementType measurementType,
    Double value
) {
}
