package com.hbelange.GymProgressTracker.service;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.hbelange.GymProgressTracker.dto.SetRequestDTO;
import com.hbelange.GymProgressTracker.dto.SetResponseDTO;
import com.hbelange.GymProgressTracker.dto.WorkoutRequestDTO;
import com.hbelange.GymProgressTracker.dto.WorkoutResponseDTO;
import com.hbelange.GymProgressTracker.entity.User;
import com.hbelange.GymProgressTracker.entity.Exercise;
import com.hbelange.GymProgressTracker.entity.Set;
import com.hbelange.GymProgressTracker.entity.Workout;
import com.hbelange.GymProgressTracker.entity.WorkoutType;
import com.hbelange.GymProgressTracker.repository.ExerciseRepository;
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
    private final ExerciseRepository exerciseRepository;

    public WorkoutServiceImpl(UserRepository userRepository, WorkoutRepository workoutRepository, SetRepository setRepository, WorkoutTypeRepository workoutTypeRepository, ExerciseRepository exerciseRepository) {
        this.userRepository = userRepository;
        this.workoutRepository = workoutRepository;
        this.setRepository = setRepository;
        this.workoutTypeRepository = workoutTypeRepository;
        this.exerciseRepository = exerciseRepository;
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
    @PreAuthorize("@workoutRepository.existsByIdAndUser_Username(#id, authentication.name)")
    public WorkoutResponseDTO getWorkoutById(Long id) {
        Workout workout = workoutRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Workout not found with id: " + id));
        return new WorkoutResponseDTO(workout.getId(), workout.getDate(), workout.getUser().getUsername(), workout.getWorkoutType().getName());
    }

    @Override
    @PreAuthorize("@workoutRepository.existsByIdAndUser_Username(#id, authentication.name)")
    public WorkoutResponseDTO updateWorkout(Long id, WorkoutRequestDTO workoutRequestDTO) {
        Workout workout = workoutRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Workout not found with id: " + id));

        WorkoutType workoutType = workoutTypeRepository.findByIdAndUser_Username(workoutRequestDTO.workoutTypeId(), workout.getUser().getUsername())
                .orElseThrow(() -> new EntityNotFoundException("Workout type not found"));

        workout.setDate(workoutRequestDTO.date());
        workout.setWorkoutType(workoutType);
        workout = workoutRepository.save(workout);
        return new WorkoutResponseDTO(workout.getId(), workout.getDate(), workout.getUser().getUsername(), workout.getWorkoutType().getName());
    }

    @Override
    @PreAuthorize("@workoutRepository.existsByIdAndUser_Username(#id, authentication.name)")
    public void deleteWorkout(Long id) {
        workoutRepository.deleteById(id);
    }

    @Override
    @PreAuthorize("@setSecurity.ownsWorkoutAndExercise(#setRequestDTO, authentication.name)")
    public SetResponseDTO addSetToWorkout(SetRequestDTO setRequestDTO) {
        Workout workout = workoutRepository.findById(setRequestDTO.workoutId())
                .orElseThrow(() -> new EntityNotFoundException("Workout not found with id: " + setRequestDTO.workoutId()));

        Exercise exercise = exerciseRepository.findById(setRequestDTO.exerciseId())
                .orElseThrow(() -> new EntityNotFoundException("Exercise not found with id: " + setRequestDTO.exerciseId()));
                
        int nextSetNumber = setRepository.countByWorkout_IdAndExercise_Id(workout.getId(), exercise.getId()) + 1;

        Set set = new Set();
        set.setWorkout(workout);
        set.setExercise(exercise);
        set.setReps(setRequestDTO.reps());
        set.setWeight(setRequestDTO.weight());
        set.setUser(workout.getUser());
        set.setSetNumber(nextSetNumber);
        set = setRepository.save(set);
        return new SetResponseDTO(
            set.getId(),
            set.getWorkout().getId(),
            set.getExercise().getId(),
            set.getExercise().getName(),
            set.getReps(),
            set.getRepsInReserve(),
            set.getWeight(),
            set.getSetNumber()
        );
    }

    @Override
    @PreAuthorize("@setSecurity.ownsSet(#setId, authentication.name)")
    public SetResponseDTO updateSet(Long setId, SetRequestDTO setRequestDTO) {
        Set set = setRepository.findById(setId)
                .orElseThrow(() -> new EntityNotFoundException("Set not found with id: " + setId));

        set.setReps(setRequestDTO.reps());
        set.setRepsInReserve(setRequestDTO.repsInReserve());
        set.setWeight(setRequestDTO.weight());
        set = setRepository.save(set);
        return new SetResponseDTO(
            set.getId(),
            set.getWorkout().getId(),
            set.getExercise().getId(),
            set.getExercise().getName(),
            set.getReps(),
            set.getRepsInReserve(),
            set.getWeight(),
            set.getSetNumber()
        );
    }

    @Override
    @PreAuthorize("@setSecurity.ownsSet(#setId, authentication.name)")
    public void deleteSet(Long setId) {
        Set set = setRepository.findById(setId)
                .orElseThrow(() -> new EntityNotFoundException("Set not found with id: " + setId));
        Long workoutId = set.getWorkout().getId();
        Long exerciseId = set.getExercise().getId();

        setRepository.deleteById(setId);

        List<Set> remainingSets = setRepository.findAllByWorkout_IdAndExercise_IdOrderBySetNumberAsc(workoutId, exerciseId);
        for (int i = 0; i < remainingSets.size(); i++) {
            remainingSets.get(i).setSetNumber(i + 1);
        }
        setRepository.saveAll(remainingSets);
    }

    @Override
    @PreAuthorize("@workoutRepository.existsByIdAndUser_Username(#workoutId, authentication.name)")
    public List<SetResponseDTO> getSetsByWorkoutId(Long workoutId) {
        List<Set> sets = setRepository.findAllByWorkout_Id(workoutId);
        return sets.stream()
                .map(set -> new SetResponseDTO(
                    set.getId(),
                    set.getWorkout().getId(),
                    set.getExercise().getId(),
                    set.getExercise().getName(),
                    set.getReps(),
                    set.getRepsInReserve(),
                    set.getWeight(),
                    set.getSetNumber()
                ))
                .toList();
    }

    @Override
    @PreAuthorize("@setSecurity.ownsSet(#setId, authentication.name)")
    public SetResponseDTO getSetById(Long setId) {
        Set set = setRepository.findById(setId)
                .orElseThrow(() -> new EntityNotFoundException("Set not found with id: " + setId));
        return new SetResponseDTO(
            set.getId(),
            set.getWorkout().getId(),
            set.getExercise().getId(),
            set.getExercise().getName(),
            set.getReps(),
            set.getRepsInReserve(),
            set.getWeight(),
            set.getSetNumber()
        );
    }
    
}
