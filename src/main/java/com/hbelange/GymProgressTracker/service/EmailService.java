package com.hbelange.GymProgressTracker.service;

import com.hbelange.GymProgressTracker.entity.User;

public interface EmailService {
    void sendVerificationEmail(User user);
}
