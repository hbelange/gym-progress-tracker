package com.hbelange.GymProgressTracker.dto;

import java.time.LocalDate;

public record ExerciseTrendPointDTO(LocalDate date, double e1rm) implements TrendPoint {
    @Override
    public Double value() {
        return e1rm;
    }
}
