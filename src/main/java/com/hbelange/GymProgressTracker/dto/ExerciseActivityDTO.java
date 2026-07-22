package com.hbelange.GymProgressTracker.dto;

import java.time.LocalDate;

public record ExerciseActivityDTO(Long exerciseId, String exerciseName, LocalDate lastWorkoutDate) {
    
}
