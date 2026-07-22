package com.hbelange.GymProgressTracker.dto;

public enum TrendRange {
    WEEK(7),
    MONTH(28);

    private final int days;

    TrendRange(int days) {
        this.days = days;
    }

    public int getDays() {
        return days;
    }
}
