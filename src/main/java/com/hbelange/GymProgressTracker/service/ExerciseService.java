package com.hbelange.GymProgressTracker.service;

import java.util.List;

import com.hbelange.GymProgressTracker.dto.ExerciseRequestDTO;
import com.hbelange.GymProgressTracker.dto.ExerciseResponseDTO;

public interface ExerciseService {
    public ExerciseResponseDTO createExercise(ExerciseRequestDTO exerciseRequestDTO, String username);

    public List<ExerciseResponseDTO> getAllExercises(String username);

    public void deleteExercise(Long exerciseId);

    public ExerciseResponseDTO updateExercise(Long exerciseId, ExerciseRequestDTO exerciseRequestDTO);
}
