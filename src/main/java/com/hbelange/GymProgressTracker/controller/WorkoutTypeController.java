package com.hbelange.GymProgressTracker.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import com.hbelange.GymProgressTracker.service.WorkoutTypeService;

import jakarta.validation.Valid;

import com.hbelange.GymProgressTracker.dto.WorkoutTypeRequestDTO;
import com.hbelange.GymProgressTracker.dto.WorkoutTypeResponseDTO;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;


@Controller
public class WorkoutTypeController {
    
    private final WorkoutTypeService workoutTypeService;

    public WorkoutTypeController(WorkoutTypeService workoutTypeService) {
        this.workoutTypeService = workoutTypeService;
    }

    @GetMapping("/workoutType")
    public String workoutTypePage() {
        return "workoutType";
    }

    @GetMapping("/api/workoutType")
    @ResponseBody
    public ResponseEntity<List<WorkoutTypeResponseDTO>> getAllWorkoutTypes(Authentication authentication) {
        List<WorkoutTypeResponseDTO> workoutTypes = workoutTypeService.getAllWorkoutTypes(Long.valueOf(authentication.getName()));
        return ResponseEntity.ok(workoutTypes);
    }

    @PutMapping("/api/workoutType/{workoutTypeId}")
    @ResponseBody
    public ResponseEntity<WorkoutTypeResponseDTO> updateWorkoutType(@PathVariable Long workoutTypeId, @Valid @RequestBody WorkoutTypeRequestDTO workoutTypeRequestDTO) {
        WorkoutTypeResponseDTO updatedWorkoutType = workoutTypeService.updateWorkoutType(workoutTypeId, workoutTypeRequestDTO);
        return ResponseEntity.ok(updatedWorkoutType);
    }

    @DeleteMapping("/api/workoutType/{workoutTypeId}")
    @ResponseBody
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWorkoutType(@PathVariable Long workoutTypeId) {
        workoutTypeService.deleteWorkoutType(workoutTypeId);
    }

    @PostMapping("/api/workoutType")
    @ResponseBody
    public ResponseEntity<WorkoutTypeResponseDTO> postMethodName(@Valid @RequestBody WorkoutTypeRequestDTO workoutTypeRequestDTO, Authentication authentication) {
        WorkoutTypeResponseDTO createdWorkoutType = workoutTypeService.createWorkoutType(workoutTypeRequestDTO, Long.valueOf(authentication.getName()));
        return ResponseEntity.status(HttpStatus.CREATED).body(createdWorkoutType);
    }
    
}
