package com.hbelange.GymProgressTracker.dto;

import jakarta.validation.constraints.Email;

public record PasswordResetDTO (
    @Email(message = "Email should be valid")
    String email
) {}
