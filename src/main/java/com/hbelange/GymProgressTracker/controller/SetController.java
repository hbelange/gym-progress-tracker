package com.hbelange.GymProgressTracker.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.hbelange.GymProgressTracker.dto.SetRequestDTO;
import com.hbelange.GymProgressTracker.dto.SetResponseDTO;
import com.hbelange.GymProgressTracker.service.WorkoutService;

import jakarta.validation.Valid;

@Controller
public class SetController {

    private final WorkoutService workoutService;

    public SetController(WorkoutService workoutService) {
        this.workoutService = workoutService;
    }

    @PostMapping("/api/sets")
    @ResponseBody
    public ResponseEntity<SetResponseDTO> addSetToWorkout(@Valid @RequestBody SetRequestDTO setRequestDTO) {
        SetResponseDTO set = workoutService.addSetToWorkout(setRequestDTO);
        return ResponseEntity.ok(set);
    }

    @GetMapping("/api/sets/{setId}")
    @ResponseBody
    public ResponseEntity<SetResponseDTO> getSetById(@PathVariable Long setId) {
        SetResponseDTO set = workoutService.getSetById(setId);
        return ResponseEntity.ok(set);
    }

    @PutMapping("/api/sets/{setId}")
    @ResponseBody
    public ResponseEntity<SetResponseDTO> updateSet(@PathVariable Long setId, @Valid @RequestBody SetRequestDTO setRequestDTO) {
        SetResponseDTO set = workoutService.updateSet(setId, setRequestDTO);
        return ResponseEntity.ok(set);
    }

    @DeleteMapping("/api/sets/{setId}")
    @ResponseBody
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSet(@PathVariable Long setId) {
        workoutService.deleteSet(setId);
    }

}
