package com.hbelange.GymProgressTracker.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hbelange.GymProgressTracker.entity.Measurement;

public interface MeasurementRepository extends JpaRepository<Measurement, Long> {

    List<Measurement> findAllByUser_Username(String username);

    boolean existsByIdAndUser_Username(Long id, String username);
    
}
