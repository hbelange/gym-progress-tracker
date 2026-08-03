package com.hbelange.GymProgressTracker.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hbelange.GymProgressTracker.dto.MeasurementTrendPointDTO;
import com.hbelange.GymProgressTracker.entity.Set;

public interface SetRepository extends JpaRepository<Set, Long> {

    boolean existsByIdAndUser_Username(Long setId, String username);

    List<Set> findAllByWorkout_Id(Long workoutId);

    List<Set> findAllByWorkout_IdAndExercise_IdOrderBySetNumberAsc(Long workoutId, Long exerciseId);

    int countByWorkout_IdAndExercise_Id(Long workoutId, Long exerciseId);

    List<Set> findAllByUser_Username(String username);

    List<Set> findAllByUser_UsernameAndExercise_Id(String username, Long exerciseId);

    Set[] findAllByUser_IdAndExercise_Id(Long userId, Long id);

    List<Set> findAllByUser_Id(Long userId);

    boolean existsByIdAndUser_Id(Long setId, Long valueOf);
}