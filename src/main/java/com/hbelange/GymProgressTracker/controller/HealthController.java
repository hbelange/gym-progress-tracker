package com.hbelange.GymProgressTracker.controller;

import com.hbelange.GymProgressTracker.security.SecurityUser;
import com.hbelange.GymProgressTracker.service.DemoAccountService;
import com.hbelange.GymProgressTracker.service.HealthService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HealthController {

    private final HealthService healthService;
    private final DemoAccountService demoAccountService;

    public HealthController(HealthService healthService, DemoAccountService demoAccountService) {
        this.healthService = healthService;
        this.demoAccountService = demoAccountService;
    }

    @GetMapping("/")
    public String home(@AuthenticationPrincipal SecurityUser user, Model model) {
        model.addAttribute("isDemo", demoAccountService.isDemoAccount(user.getUsername()));
        return "home";
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("demoUsername", demoAccountService.getDemoUsername());
        model.addAttribute("demoPassword", demoAccountService.getDemoPassword());
        return "login";
    }

    @GetMapping("/api/health")
    @ResponseBody
    public String getHealth() {
        return healthService.getHealth();
    }
}