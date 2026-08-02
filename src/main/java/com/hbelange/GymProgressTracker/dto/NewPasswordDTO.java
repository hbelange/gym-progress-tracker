package com.hbelange.GymProgressTracker.dto;

import jakarta.validation.constraints.NotBlank;

public record NewPasswordDTO (
    @NotBlank(message = "Token is required")
    String token,
    @NotBlank(message = "Password is required")
    String password,
    @NotBlank(message = "Confirm password is required")
    String confirmPassword
) {}
