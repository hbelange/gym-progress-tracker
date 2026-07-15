package com.hbelange.GymProgressTracker.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("userRequestDTO", new UserRequestDTO("", ""));
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

        return "redirect:/login?registered";
    }

}
