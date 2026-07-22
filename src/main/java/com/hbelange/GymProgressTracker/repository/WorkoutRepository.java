package com.hbelange.GymProgressTracker.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hbelange.GymProgressTracker.entity.Workout;

public interface WorkoutRepository extends JpaRepository<Workout, Long> {

    List<Workout> findAllByUser_Username(String username);

    boolean existsByIdAndUser_Username(Long id, String username);

}
