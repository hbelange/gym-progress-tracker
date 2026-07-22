package com.hbelange.GymProgressTracker.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hbelange.GymProgressTracker.entity.Measurement;
import com.hbelange.GymProgressTracker.entity.MeasurementType;

public interface MeasurementRepository extends JpaRepository<Measurement, Long> {

    List<Measurement> findAllByUser_Username(String username);

    boolean existsByIdAndUser_Username(Long id, String username);

    List<Measurement> findAllByUser_UsernameAndTypeAndDateBetween(String username,
            MeasurementType type, LocalDate startDate, LocalDate endDate);
    
}
