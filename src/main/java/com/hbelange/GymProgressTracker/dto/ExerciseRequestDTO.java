package com.hbelange.GymProgressTracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ExerciseRequestDTO (
    @NotBlank(message = "Exercise name cannot be blank")
    @Size(min = 1, max = 100, message = "Exercise name must be between 1 and 100 characters")
    String name
){}
