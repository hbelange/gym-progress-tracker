package com.hbelange.GymProgressTracker.entity;

public enum MeasurementType {
    WEIGHT,
    CALORIES,
    STEPS;

    public static MeasurementType fromString(String text) {
        if (text != null) {
            for (MeasurementType type : MeasurementType.values()) {
                if (text.equalsIgnoreCase(type.name())) {
                    return type;
                }
            }
        }
        throw new IllegalArgumentException("No measurement type found for: " + text);
    }
}
