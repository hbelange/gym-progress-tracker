package com.hbelange.GymProgressTracker.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hbelange.GymProgressTracker.entity.Workout;

import jakarta.validation.constraints.NotNull;

public interface WorkoutRepository extends JpaRepository<Workout, Long> {

    List<Workout> findAllByUser_Username(String username);

    boolean existsByIdAndUser_Username(Long id, String username);

    List<Workout> findAllByUser_Id(Long userId);

    boolean existsByIdAndUser_Id(Long workoutId, Long userId);

    void deleteAllByUser_Id(Long userId);

}
