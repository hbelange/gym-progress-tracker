package com.hbelange.GymProgressTracker.controller;

import com.hbelange.GymProgressTracker.service.HealthService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HealthController {

    private final HealthService healthService;

    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("dbStatus", healthService.getHealth());
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/api/health")
    @ResponseBody
    public String getHealth() {
        return healthService.getHealth();
    }
}