package com.hbelange.GymProgressTracker.dto;

import jakarta.validation.constraints.NotBlank;

public record UsernameUpdateDTO (
    @NotBlank(message = "New username cannot be blank")
    String newUsername
) {}
