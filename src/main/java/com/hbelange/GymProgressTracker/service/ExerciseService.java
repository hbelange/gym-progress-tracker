package com.hbelange.GymProgressTracker.service;

import java.util.List;

import com.hbelange.GymProgressTracker.dto.ExerciseRequestDTO;
import com.hbelange.GymProgressTracker.dto.ExerciseResponseDTO;

public interface ExerciseService {
    public ExerciseResponseDTO createExercise(ExerciseRequestDTO exerciseRequestDTO, Long userId);

    public List<ExerciseResponseDTO> getAllExercises(Long userId);

    public void deleteExercise(Long exerciseId);

    public ExerciseResponseDTO updateExercise(Long exerciseId, ExerciseRequestDTO exerciseRequestDTO);
}
