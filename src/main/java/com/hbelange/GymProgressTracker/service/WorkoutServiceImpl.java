package com.hbelange.GymProgressTracker.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hbelange.GymProgressTracker.dto.SetRequestDTO;
import com.hbelange.GymProgressTracker.dto.SetResponseDTO;
import com.hbelange.GymProgressTracker.dto.WorkoutRequestDTO;
import com.hbelange.GymProgressTracker.dto.WorkoutResponseDTO;
import com.hbelange.GymProgressTracker.entity.User;
import com.hbelange.GymProgressTracker.entity.Workout;
import com.hbelange.GymProgressTracker.entity.WorkoutType;
import com.hbelange.GymProgressTracker.repository.SetRepository;
import com.hbelange.GymProgressTracker.repository.UserRepository;
import com.hbelange.GymProgressTracker.repository.WorkoutRepository;
import com.hbelange.GymProgressTracker.repository.WorkoutTypeRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class WorkoutServiceImpl implements WorkoutService {

    private final UserRepository userRepository;
    private final WorkoutRepository workoutRepository;
    private final SetRepository setRepository;
    private final WorkoutTypeRepository workoutTypeRepository;

    public WorkoutServiceImpl(UserRepository userRepository, WorkoutRepository workoutRepository, SetRepository setRepository, WorkoutTypeRepository workoutTypeRepository) {
        this.userRepository = userRepository;
        this.workoutRepository = workoutRepository;
        this.setRepository = setRepository;
        this.workoutTypeRepository = workoutTypeRepository;
    }

    @Override
    public WorkoutResponseDTO createWorkout(WorkoutRequestDTO workoutRequestDTO, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + username));

        WorkoutType workoutType = workoutTypeRepository.findByIdAndUser_Username(workoutRequestDTO.workoutTypeId(), username)
                .orElseThrow(() -> new EntityNotFoundException("Workout type not found"));

        Workout workout = new Workout();
        workout.setDate(workoutRequestDTO.date());
        workout.setUser(user);
        workout.setWorkoutType(workoutType);
        workout = workoutRepository.save(workout);
        return new WorkoutResponseDTO(workout.getId(), workout.getDate(), workout.getUser().getUsername(), workout.getWorkoutType().getName());
    }

    @Override
    public List<WorkoutResponseDTO> getAllWorkouts(String username) {
        List<Workout> workouts = workoutRepository.findAllByUser_Username(username);
        return workouts.stream()
                .map(workout -> new WorkoutResponseDTO(workout.getId(), workout.getDate(), workout.getUser().getUsername(), workout.getWorkoutType().getName()))
                .toList();
    }

    @Override
    public WorkoutResponseDTO getWorkoutById(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getWorkoutById'");
    }

    @Override
    public WorkoutResponseDTO updateWorkout(Long id, WorkoutRequestDTO workoutRequestDTO) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateWorkout'");
    }

    @Override
    public WorkoutResponseDTO deleteWorkout(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteWorkout'");
    }

    @Override
    public SetResponseDTO addSetToWorkout(SetRequestDTO setRequestDTO) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addSetToWorkout'");
    }

    @Override
    public SetResponseDTO updateSet(Long setId, SetRequestDTO setRequestDTO) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateSet'");
    }

    @Override
    public SetResponseDTO deleteSet(Long setId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteSet'");
    }

    @Override
    public List<SetResponseDTO> getSetsByWorkoutId(Long workoutId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSetsByWorkoutId'");
    }

    @Override
    public SetResponseDTO getSetById(Long setId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSetById'");
    }
    
}
