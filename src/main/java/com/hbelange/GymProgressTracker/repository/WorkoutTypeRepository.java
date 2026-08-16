package com.hbelange.GymProgressTracker.repository;

import com.hbelange.GymProgressTracker.entity.WorkoutType;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutTypeRepository extends JpaRepository<WorkoutType, Long> {
    List<WorkoutType> findAllByUser_Username(String username);

    boolean existsByIdAndUser_Id(Long id, Long userId);

    Optional<WorkoutType> findByIdAndUser_Id(Long id, Long userId);

    List<WorkoutType> findAllByUser_Id(Long userId);

    void deleteAllByUser_Id(Long userId);
}
