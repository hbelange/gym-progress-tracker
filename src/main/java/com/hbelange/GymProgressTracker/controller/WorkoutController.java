package com.hbelange.GymProgressTracker.controller;

import java.util.List;

import org.springframework.security.core.Authentication; 
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hbelange.GymProgressTracker.dto.WorkoutRequestDTO;
import com.hbelange.GymProgressTracker.dto.WorkoutResponseDTO;
import com.hbelange.GymProgressTracker.service.WorkoutService;

import jakarta.validation.Valid;

@Controller
public class WorkoutController {
    
    private final WorkoutService workoutService;

    public WorkoutController(WorkoutService workoutService) {
        this.workoutService = workoutService;
    }

    @GetMapping("/api/workouts")
    @ResponseBody
    public ResponseEntity<List<WorkoutResponseDTO>> getAllWorkouts(Authentication authentication) {
        List<WorkoutResponseDTO> workouts = workoutService.getAllWorkouts(authentication.getName());
        return ResponseEntity.ok(workouts);
    }

    @PostMapping("/api/workouts")
    @ResponseBody
    public ResponseEntity<WorkoutResponseDTO> createWorkout(@Valid @RequestBody WorkoutRequestDTO workoutRequestDTO, Authentication authentication) {
        WorkoutResponseDTO workout = workoutService.createWorkout(workoutRequestDTO, authentication.getName());
        return ResponseEntity.ok(workout);
    }

}
