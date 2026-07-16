package com.hbelange.GymProgressTracker.service;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.hbelange.GymProgressTracker.dto.WorkoutTypeRequestDTO;
import com.hbelange.GymProgressTracker.dto.WorkoutTypeResponseDTO;
import com.hbelange.GymProgressTracker.repository.UserRepository;
import com.hbelange.GymProgressTracker.repository.WorkoutTypeRepository;
import com.hbelange.GymProgressTracker.entity.User;
import com.hbelange.GymProgressTracker.entity.WorkoutType;

import jakarta.persistence.EntityNotFoundException;

@Service
public class WorkoutTypeServiceImpl implements WorkoutTypeService {

    private final WorkoutTypeRepository workoutTypeRepository;
    private final UserRepository userRepository;

    public WorkoutTypeServiceImpl(WorkoutTypeRepository workoutTypeRepository, UserRepository userRepository) {
        this.workoutTypeRepository = workoutTypeRepository;
        this.userRepository = userRepository;
    }

    @Override
    public WorkoutTypeResponseDTO createWorkoutType(WorkoutTypeRequestDTO workoutTypeRequestDTO, String username) {
        User owner = userRepository.findByUsername(username)
            .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));
        
        WorkoutType workoutType = new WorkoutType();
        workoutType.setName(workoutTypeRequestDTO.name());
        workoutType.setUser(owner);
        workoutType = workoutTypeRepository.save(workoutType);
        return new WorkoutTypeResponseDTO(workoutType.getId(), workoutType.getName());
    }

    @Override
    public List<WorkoutTypeResponseDTO> getAllWorkoutTypes(String username) {
        
        List<WorkoutType> workoutTypes = workoutTypeRepository.findAllByUser_Username(username);
        return workoutTypes.stream()
                .map(workoutType -> new WorkoutTypeResponseDTO(workoutType.getId(), workoutType.getName()))
                .toList();        
    }

    @Override
    @PreAuthorize("@workoutTypeRepository.existsByIdAndUser_Username(#id, authentication.name)")
    public WorkoutTypeResponseDTO updateWorkoutType(Long id, WorkoutTypeRequestDTO workoutTypeRequestDTO) {
        WorkoutType workoutType = workoutTypeRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Workout type not found with id: " + id));
        
        workoutType.setName(workoutTypeRequestDTO.name());
        workoutType = workoutTypeRepository.save(workoutType);
        return new WorkoutTypeResponseDTO(workoutType.getId(), workoutType.getName());
    }

    @Override
    @PreAuthorize("@workoutTypeRepository.existsByIdAndUser_Username(#id, authentication.name)")
    public void deleteWorkoutType(Long id) {
        workoutTypeRepository.deleteById(id);
    }
    
}
