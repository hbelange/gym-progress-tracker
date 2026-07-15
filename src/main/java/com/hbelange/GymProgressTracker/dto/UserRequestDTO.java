package com.hbelange.GymProgressTracker.dto;

import jakarta.validation.constraints.NotBlank;

public record UserRequestDTO (
    @NotBlank(message = "Username cannot be blank")
    String username,
    @NotBlank(message = "Password cannot be blank")
    String password
) {}