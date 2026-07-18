package com.hbelange.GymProgressTracker.dto;

import java.time.LocalDate;

public record WorkoutResponseDTO (
    Long id,
    LocalDate date,
    String username,
    String workoutTypeName
) {}
