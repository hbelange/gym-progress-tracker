package com.hbelange.GymProgressTracker.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.hbelange.GymProgressTracker.dto.ExerciseRequestDTO;
import com.hbelange.GymProgressTracker.dto.ExerciseResponseDTO;
import com.hbelange.GymProgressTracker.security.SecurityUser;
import com.hbelange.GymProgressTracker.service.ExerciseService;

import org.springframework.http.HttpStatus;

import jakarta.validation.Valid;

@Controller
public class ExerciseController {

    private final ExerciseService exerciseService;

    public ExerciseController(ExerciseService exerciseService) {
        this.exerciseService = exerciseService;
    }

    @GetMapping("/exercise")
    public String exercisePage() {
        return "exercise";
    }

    @PostMapping("/api/exercise")
    @ResponseBody
    public ResponseEntity<ExerciseResponseDTO> createExercise(@Valid @RequestBody ExerciseRequestDTO exerciseRequestDTO, @AuthenticationPrincipal SecurityUser user) {
        ExerciseResponseDTO exercise = exerciseService.createExercise(exerciseRequestDTO, user.getId());
        return ResponseEntity.ok(exercise);
    }

    @GetMapping("/api/exercise")
    @ResponseBody
    public ResponseEntity<List<ExerciseResponseDTO>> getAllExercises(@AuthenticationPrincipal SecurityUser user) {
        List<ExerciseResponseDTO> exercises = exerciseService.getAllExercises(user.getId());
        return ResponseEntity.ok(exercises);
    }

    @PutMapping("/api/exercise/{exerciseId}")
    @ResponseBody
    public ResponseEntity<ExerciseResponseDTO> updateExercise(@PathVariable Long exerciseId, @Valid @RequestBody ExerciseRequestDTO exerciseRequestDTO) {
        ExerciseResponseDTO updatedExercise = exerciseService.updateExercise(exerciseId, exerciseRequestDTO);
        return ResponseEntity.ok(updatedExercise);
    }

    @DeleteMapping("/api/exercise/{exerciseId}")
    @ResponseBody
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExercise(@PathVariable Long exerciseId) {
        exerciseService.deleteExercise(exerciseId);
    }
}
