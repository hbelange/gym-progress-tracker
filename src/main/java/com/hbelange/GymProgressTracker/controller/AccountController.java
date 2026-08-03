package com.hbelange.GymProgressTracker.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hbelange.GymProgressTracker.security.SecurityUser;
import com.hbelange.GymProgressTracker.service.UserService;

import jakarta.validation.Valid;

import com.hbelange.GymProgressTracker.dto.AccountResponseDTO;
import com.hbelange.GymProgressTracker.dto.EmailChangeRequestDTO;
import com.hbelange.GymProgressTracker.dto.UsernameUpdateDTO;

@Controller
public class AccountController {
    
    private final UserService userService;

    public AccountController(UserService userService) {
        this.userService = userService;
    }

    // REST endpoints for account management

    @GetMapping("/api/account")
    @ResponseBody
    public ResponseEntity<AccountResponseDTO> getAccount(@AuthenticationPrincipal SecurityUser user) {
        // Implementation for retrieving account details
        AccountResponseDTO account = userService.getAccount(user.getId());
        return ResponseEntity.ok(account);
    }

    @PutMapping("/api/account/username")
    @ResponseBody
    public ResponseEntity<AccountResponseDTO> updateUsername(@Valid @RequestBody UsernameUpdateDTO usernameUpdateDTO, @AuthenticationPrincipal SecurityUser user) {
        // Implementation for updating username
        AccountResponseDTO updatedAccount = userService.updateUsername(user.getId(), usernameUpdateDTO.newUsername());
        return ResponseEntity.ok(updatedAccount);
    }

    @PostMapping("/api/account/email")
    @ResponseBody
    public ResponseEntity<AccountResponseDTO> changeEmail(@Valid @RequestBody EmailChangeRequestDTO emailChangeRequestDTO, @AuthenticationPrincipal SecurityUser user) {
        // Implementation for changing email
        AccountResponseDTO updatedAccount = userService.changeEmail(user.getId(), emailChangeRequestDTO.newEmail());
        return ResponseEntity.ok(updatedAccount);
    }

    @DeleteMapping("/api/account/email")
    @ResponseBody
    public ResponseEntity<AccountResponseDTO> cancelEmailChange(@AuthenticationPrincipal SecurityUser user) {
        // Implementation for canceling email change
        AccountResponseDTO updatedAccount = userService.cancelEmailChange(user.getId());
        return ResponseEntity.ok(updatedAccount);
    }
    
    // MVC endpoints for account management pages

    @GetMapping("/account")
    public String accountPage() {
        return "account";
    }

    @GetMapping("/change-email")
    public String changeEmail(@RequestParam String token) {
        // If token is valid, the email change will be confirmed and the user will be redirected to the account page with a success message.
        // If the token is invalid or expired, the user will be redirected to the account page with an error message.
        try {
            userService.handleEmailChangeVerification(token);
            return "redirect:/account?emailChangeVerified"; // Redirect to account page with a success message
        } catch (RuntimeException e) {
            return "redirect:/account?error=" + e.getMessage(); // Redirect to account page with an error message
        }
    }

}
