package com.hbelange.GymProgressTracker.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.hbelange.GymProgressTracker.dto.ExerciseRequestDTO;
import com.hbelange.GymProgressTracker.dto.ExerciseResponseDTO;
import com.hbelange.GymProgressTracker.service.ExerciseService;

import org.springframework.http.HttpStatus;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/exercise")
public class ExerciseController {
    
    private final ExerciseService exerciseService;

    public ExerciseController(ExerciseService exerciseService) {
        this.exerciseService = exerciseService;
    }

    @PostMapping
    public ResponseEntity<ExerciseResponseDTO> createExercise(@Valid @RequestBody ExerciseRequestDTO exerciseRequestDTO, Authentication authentication) {
        ExerciseResponseDTO exercise = exerciseService.createExercise(exerciseRequestDTO, authentication.getName());
        return ResponseEntity.ok(exercise);
    }

    @GetMapping
    public ResponseEntity<List<ExerciseResponseDTO>> getAllExercises(Authentication authentication) {
        List<ExerciseResponseDTO> exercises = exerciseService.getAllExercises(authentication.getName());
        return ResponseEntity.ok(exercises);
    }

    @PutMapping("/{exerciseId}")
    public ResponseEntity<ExerciseResponseDTO> updateExercise(@PathVariable Long exerciseId, @Valid @RequestBody ExerciseRequestDTO exerciseRequestDTO) {
        ExerciseResponseDTO updatedExercise = exerciseService.updateExercise(exerciseId, exerciseRequestDTO);
        return ResponseEntity.ok(updatedExercise);
    }

    @DeleteMapping("/{exerciseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExercise(@PathVariable Long exerciseId) {
        exerciseService.deleteExercise(exerciseId);
    }
}
