package com.hbelange.GymProgressTracker.service;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.hbelange.GymProgressTracker.dto.ExerciseRequestDTO;
import com.hbelange.GymProgressTracker.dto.ExerciseResponseDTO;
import com.hbelange.GymProgressTracker.entity.Exercise;
import com.hbelange.GymProgressTracker.entity.User;
import com.hbelange.GymProgressTracker.repository.ExerciseRepository;
import com.hbelange.GymProgressTracker.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ExerciseServiceImpl implements ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final UserRepository userRepository;

    public ExerciseServiceImpl(ExerciseRepository exerciseRepository, UserRepository userRepository) {
        this.exerciseRepository = exerciseRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ExerciseResponseDTO createExercise(ExerciseRequestDTO exerciseRequestDTO, Long userId) {
        User owner = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        Exercise exercise = new Exercise();
        exercise.setName(exerciseRequestDTO.name());
        exercise.setUser(owner);
        exercise = exerciseRepository.save(exercise);
        return new ExerciseResponseDTO(exercise.getId(), exercise.getName());
    }

    @Override
    public List<ExerciseResponseDTO> getAllExercises(Long userId) {
        List<Exercise> exercises = exerciseRepository.findAllByUser_Id(userId);
        List<ExerciseResponseDTO> exerciseResponseDTOs = exercises.stream()
                .map(exercise -> new ExerciseResponseDTO(exercise.getId(), exercise.getName()))
                .toList();
        return exerciseResponseDTOs;
    }
    
    @Override
    @PreAuthorize("@exerciseRepository.existsByIdAndUser_Id(#exerciseId, authentication.principal.id)")
    public void deleteExercise(Long exerciseId) {
        exerciseRepository.deleteById(exerciseId);
    }

    @Override
    @PreAuthorize("@exerciseRepository.existsByIdAndUser_Id(#exerciseId, authentication.principal.id)")
    public ExerciseResponseDTO updateExercise(Long exerciseId, ExerciseRequestDTO exerciseRequestDTO) {
        Exercise exercise = exerciseRepository.findById(exerciseId)
            .orElseThrow(() -> new EntityNotFoundException("Exercise not found with id: " + exerciseId));
        exercise.setName(exerciseRequestDTO.name());
        exercise = exerciseRepository.save(exercise);
        return new ExerciseResponseDTO(exercise.getId(), exercise.getName());
    }

}