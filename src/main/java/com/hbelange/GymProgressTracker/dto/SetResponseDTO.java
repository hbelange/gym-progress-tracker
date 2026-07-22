package com.hbelange.GymProgressTracker.dto;

public record SetResponseDTO (
    Long id,
    Long workoutId,
    Long exerciseId,
    String exerciseName,
    Integer reps,
    Integer repsInReserve,
    Double weight,
    Integer setNumber
) {}
