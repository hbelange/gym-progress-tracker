package com.hbelange.GymProgressTracker.security;

import com.hbelange.GymProgressTracker.repository.SetRepository;
import org.springframework.stereotype.Component;

import com.hbelange.GymProgressTracker.dto.SetRequestDTO;
import com.hbelange.GymProgressTracker.repository.ExerciseRepository;
import com.hbelange.GymProgressTracker.repository.WorkoutRepository;

@Component
public class SetSecurity {

    private final SetRepository setRepository;
    private final WorkoutRepository workoutRepository;
    private final ExerciseRepository exerciseRepository;

    public SetSecurity(WorkoutRepository workoutRepository, ExerciseRepository exerciseRepository, SetRepository setRepository) {
        this.workoutRepository = workoutRepository;
        this.exerciseRepository = exerciseRepository;
        this.setRepository = setRepository;
    }

    /** True if the given user owns both the workout and the exercise referenced by the set request. */
    public boolean ownsWorkoutAndExercise(SetRequestDTO setRequestDTO, String username) {
        return workoutRepository.existsByIdAndUser_Username(setRequestDTO.workoutId(), username)
                && exerciseRepository.existsByIdAndUser_Username(setRequestDTO.exerciseId(), username);
    }

    public boolean ownsSet(Long setId, String username) {
        return setRepository.existsByIdAndUser_Username(setId, username);
    }
}
