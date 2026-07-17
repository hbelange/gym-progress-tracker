package com.hbelange.GymProgressTracker.dto;

import java.time.LocalDate;

import com.hbelange.GymProgressTracker.entity.MeasurementType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

public record MeasurementRequestDTO(

    @NotNull(message = "Date is required")
    @PastOrPresent(message = "Date cannot be in the future")
    LocalDate date,

    @NotNull(message = "Measurement type is required")
    MeasurementType measurementType,

    @NotNull(message = "Value is required")
    @Positive(message = "Value must be greater than zero")
    Double value
) {}