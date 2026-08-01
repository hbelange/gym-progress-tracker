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

    

}
