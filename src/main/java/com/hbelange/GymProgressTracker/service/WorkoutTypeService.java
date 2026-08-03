package com.hbelange.GymProgressTracker.service;

import java.util.List;

import com.hbelange.GymProgressTracker.dto.WorkoutTypeRequestDTO;
import com.hbelange.GymProgressTracker.dto.WorkoutTypeResponseDTO;

public interface WorkoutTypeService {

    public WorkoutTypeResponseDTO createWorkoutType(WorkoutTypeRequestDTO workoutTypeRequestDTO, Long userId);

    public List<WorkoutTypeResponseDTO> getAllWorkoutTypes(Long userId);

    public WorkoutTypeResponseDTO updateWorkoutType(Long id, WorkoutTypeRequestDTO workoutTypeRequestDTO);

    public void deleteWorkoutType(Long id);
}
