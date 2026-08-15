package com.hbelange.GymProgressTracker.service;

import com.hbelange.GymProgressTracker.dto.AccountResponseDTO;
import com.hbelange.GymProgressTracker.dto.NewPasswordDTO;
import com.hbelange.GymProgressTracker.dto.UserRequestDTO;
import com.hbelange.GymProgressTracker.dto.UserResponseDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public interface UserService {
    public UserResponseDTO registerUser(UserRequestDTO userRequestDTO);
    public void handleVerification(String token);
    public void resendVerificationEmail(String username);
    public void handlePasswordReset(String email);
    public void validatePasswordResetToken(String token);
    public void resetPassword(NewPasswordDTO newPasswordDTO);
    public AccountResponseDTO getAccount(Long userId);
    public AccountResponseDTO updateUsername(Long userId,
            String newUsername);
    public AccountResponseDTO changeEmail(Long userId, String newEmail);
    public AccountResponseDTO cancelEmailChange(Long userId);
    public void handleEmailChangeVerification(String token);
}
