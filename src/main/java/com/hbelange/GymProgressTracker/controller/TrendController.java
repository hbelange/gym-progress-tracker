package com.hbelange.GymProgressTracker.controller;

import org.springframework.stereotype.Controller;

import com.hbelange.GymProgressTracker.service.TrendService;

@Controller
public class TrendController {
    
    private final TrendService trendService;

    public TrendController(TrendService trendService) {
        this.trendService = trendService;
    }
}
