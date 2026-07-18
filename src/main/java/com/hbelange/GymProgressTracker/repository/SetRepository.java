package com.hbelange.GymProgressTracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hbelange.GymProgressTracker.entity.Set;

public interface SetRepository extends JpaRepository<Set, Long> {
    
}
