package com.hbelange.GymProgressTracker.service;

import com.hbelange.GymProgressTracker.dto.UserRequestDTO;
import com.hbelange.GymProgressTracker.dto.UserResponseDTO;

public interface UserService {
    public UserResponseDTO registerUser(UserRequestDTO userRequestDTO);
    public void handleVerification(String token);
    public void resendVerificationEmail(String username);
}
