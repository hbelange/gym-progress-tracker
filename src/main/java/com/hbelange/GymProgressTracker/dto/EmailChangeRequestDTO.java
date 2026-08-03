package com.hbelange.GymProgressTracker.dto;

import jakarta.validation.constraints.Email;

public record EmailChangeRequestDTO (
    @Email(message = "New email must be a valid email address")
    String newEmail
) {}
