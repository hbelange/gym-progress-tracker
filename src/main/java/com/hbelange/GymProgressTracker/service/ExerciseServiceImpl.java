package com.hbelange.GymProgressTracker.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
    public ExerciseResponseDTO createExercise(ExerciseRequestDTO exerciseRequestDTO, String username) {
        User owner = userRepository.findByUsername(username)
            .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));
        Exercise exercise = new Exercise();
        exercise.setName(exerciseRequestDTO.name());
        exercise.setUser(owner);
        exercise = exerciseRepository.save(exercise);
        return new ExerciseResponseDTO(exercise.getId(), exercise.getName());
    }

    @Override
    public List<ExerciseResponseDTO> getAllExercises(String username) {
        List<Exercise> exercises = exerciseRepository.findAllByUser_Username(username);
        List<ExerciseResponseDTO> exerciseResponseDTOs = exercises.stream()
                .map(exercise -> new ExerciseResponseDTO(exercise.getId(), exercise.getName()))
                .toList();
        return exerciseResponseDTOs;
    }
    
    @Override
    @PreAuthorize("@exerciseRepository.existsByIdAndUser_Username(#exerciseId, authentication.name)")
    public void deleteExercise(Long exerciseId) {
        exerciseRepository.deleteById(exerciseId);
    }

    @Override
    @PreAuthorize("@exerciseRepository.existsByIdAndUser_Username(#exerciseId, authentication.name)")
    public ExerciseResponseDTO updateExercise(Long exerciseId, ExerciseRequestDTO exerciseRequestDTO) {
        Exercise exercise = exerciseRepository.findById(exerciseId)
            .orElseThrow(() -> new EntityNotFoundException("Exercise not found with id: " + exerciseId));
        exercise.setName(exerciseRequestDTO.name());
        exercise = exerciseRepository.save(exercise);
        return new ExerciseResponseDTO(exercise.getId(), exercise.getName());
    }

}