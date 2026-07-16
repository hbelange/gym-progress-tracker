package com.hbelange.GymProgressTracker.repository;

import com.hbelange.GymProgressTracker.entity.WorkoutType;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutTypeRepository extends JpaRepository<WorkoutType, Long> {
    List<WorkoutType> findAllByUser_Username(String username);

    boolean existsByIdAndUser_Username(Long id, String username);
}
