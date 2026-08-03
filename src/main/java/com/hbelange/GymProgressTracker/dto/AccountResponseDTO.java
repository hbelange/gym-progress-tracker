package com.hbelange.GymProgressTracker.dto;

public record AccountResponseDTO (
    String username,
    String email,
    String pendingEmail
) {}
