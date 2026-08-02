package com.hbelange.GymProgressTracker.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hbelange.GymProgressTracker.dto.NewPasswordDTO;
import com.hbelange.GymProgressTracker.dto.PasswordResetDTO;
import com.hbelange.GymProgressTracker.dto.UserRequestDTO;
import com.hbelange.GymProgressTracker.dto.UserResponseDTO;
import com.hbelange.GymProgressTracker.exception.UserAlreadyExistsException;
import com.hbelange.GymProgressTracker.service.UserService;

import jakarta.validation.Valid;

@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/api/register")
    @ResponseBody
    public ResponseEntity<UserResponseDTO> registerUser(@Valid @RequestBody UserRequestDTO userRequestDTO) {
        UserResponseDTO registeredUser = userService.registerUser(userRequestDTO);
        return ResponseEntity.ok(registeredUser);
    }

    @GetMapping("/verify")
    public String verifyEmail(@RequestParam String token) {
        try {
            userService.handleVerification(token);
            return "redirect:/login?verified"; // Redirect to login page with a verified message
        } catch (RuntimeException e) {
            return "redirect:/login?error=" + e.getMessage(); // Redirect to login page with an error message
        }
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam String token, Model model) {
        try {
            // Here you might want to validate the token before showing the form
            userService.validatePasswordResetToken(token); // This is just an example; you might have a separate method for token validation
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "reset-password"; // Redirect to an error page or show an error message
        }
        model.addAttribute("token", token);
        return "reset-password";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("userRequestDTO", new UserRequestDTO("", "", ""));
        return "register";
    }

    @PostMapping("/register")
    public String registerFromForm(@Valid @ModelAttribute UserRequestDTO userRequestDTO, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Please fill in all required fields.");
            return "register";
        }

        try {
            userService.registerUser(userRequestDTO);
        } catch (UserAlreadyExistsException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }

        return "redirect:/register/pending";
    }

    @GetMapping("/register/pending")
    public String showPendingVerification(Model model) {
        model.addAttribute("message", "Please check your email for a verification link.");
        return "pending";
    }

    @PostMapping("/resend-verification")
    public String resendVerification(@RequestParam String username) {
        userService.resendVerificationEmail(username);
        return "redirect:/register/pending";
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String resetPassword(@Valid @ModelAttribute PasswordResetDTO passwordResetDTO, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Please enter a valid email address.");
            return "forgot-password";
        }
        userService.handlePasswordReset(passwordResetDTO.email());
        return "redirect:/forgot-password?sent"; // Redirect to a page indicating that the reset email has been sent
    }

    @PostMapping("/reset-password")
    public String resetPasswordFromForm(@Valid @ModelAttribute NewPasswordDTO newPasswordDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "redirect:/reset-password?token=" + newPasswordDTO.token() + "&error=Please fill in all required fields.";
        }
        try {
            userService.resetPassword(newPasswordDTO);
        } catch (RuntimeException e) {
            return "redirect:/reset-password?token=" + newPasswordDTO.token() + "&error=" + e.getMessage();
        }
        return "redirect:/login?passwordReset"; // Redirect to login page with a password reset success message
    }

}
