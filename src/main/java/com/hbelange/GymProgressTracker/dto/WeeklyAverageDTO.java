package com.hbelange.GymProgressTracker.dto;

import java.time.LocalDate;

public record WeeklyAverageDTO(LocalDate weekStart, Double average) {}
