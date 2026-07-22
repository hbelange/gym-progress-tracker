package com.hbelange.GymProgressTracker.dto;

import java.time.LocalDate;

public record MeasurementTrendPointDTO(LocalDate date, Double value) implements TrendPoint {}
