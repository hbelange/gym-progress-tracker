package com.hbelange.GymProgressTracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserRequestDTO (
    @Email(message = "Email should be valid")
    String email,
    @NotBlank(message = "Username cannot be blank")
    String username,
    @NotBlank(message = "Password cannot be blank")
    String password
) {}