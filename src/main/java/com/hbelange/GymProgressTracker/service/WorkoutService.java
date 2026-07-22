package com.hbelange.GymProgressTracker.service;

import java.util.List;

import com.hbelange.GymProgressTracker.dto.SetRequestDTO;
import com.hbelange.GymProgressTracker.dto.SetResponseDTO;
import com.hbelange.GymProgressTracker.dto.WorkoutRequestDTO;
import com.hbelange.GymProgressTracker.dto.WorkoutResponseDTO;

public interface WorkoutService {

    WorkoutResponseDTO createWorkout(WorkoutRequestDTO workoutRequestDTO, String username);

    List<WorkoutResponseDTO> getAllWorkouts(String username);

    WorkoutResponseDTO getWorkoutById(Long id);

    WorkoutResponseDTO updateWorkout(Long id, WorkoutRequestDTO workoutRequestDTO);

    void deleteWorkout(Long id);

    SetResponseDTO addSetToWorkout(SetRequestDTO setRequestDTO);

    SetResponseDTO updateSet(Long setId, SetRequestDTO setRequestDTO);

    void deleteSet(Long setId);

    List<SetResponseDTO> getSetsByWorkoutId(Long workoutId);

    SetResponseDTO getSetById(Long setId);

}
