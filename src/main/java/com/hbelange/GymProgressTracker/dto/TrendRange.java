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

    public static TrendRange fromString(String range) {
        if (range != null) {
            for (TrendRange t : TrendRange.values()) {
                if (range.equalsIgnoreCase(t.name())) {
                    return t;
                }
            }
        }
        throw new IllegalArgumentException("No trend range found for: " + range);
    }
}
