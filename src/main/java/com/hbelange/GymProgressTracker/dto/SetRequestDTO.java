package com.hbelange.GymProgressTracker.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record SetRequestDTO (
    @NotNull( message = "Workout ID cannot be null")
    Long workoutId,

    @NotNull(message = "Exercise ID cannot be null")
    Long exerciseId,

    @NotNull(message = "Reps cannot be null")
    @PositiveOrZero(message = "Reps must be greater than or equal to zero")
    Integer reps,

    @PositiveOrZero(message = "Reps in reserve must be greater than or equal to zero")
    Integer repsInReserve,

    @NotNull(message = "Weight cannot be null")
    @PositiveOrZero(message = "Weight must be greater than or equal to zero")
    Double weight
) {}
