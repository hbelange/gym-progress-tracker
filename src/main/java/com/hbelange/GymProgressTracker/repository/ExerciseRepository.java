package com.hbelange.GymProgressTracker.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hbelange.GymProgressTracker.entity.Exercise;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    List<Exercise> findAllByUser_Username(String username);

    boolean existsByIdAndUser_Username(Long id, String username);

}
