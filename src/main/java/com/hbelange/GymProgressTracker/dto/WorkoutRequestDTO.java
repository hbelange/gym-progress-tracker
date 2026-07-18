package com.hbelange.GymProgressTracker.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public record WorkoutRequestDTO (
    @NotNull(message = "Date is required")
    LocalDate date,
    @NotNull(message = "Workout type is required")
    Long workoutTypeId
) {}
