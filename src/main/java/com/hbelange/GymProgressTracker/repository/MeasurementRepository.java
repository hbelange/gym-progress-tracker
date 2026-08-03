package com.hbelange.GymProgressTracker.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hbelange.GymProgressTracker.entity.Measurement;
import com.hbelange.GymProgressTracker.entity.MeasurementType;

public interface MeasurementRepository extends JpaRepository<Measurement, Long> {

    List<Measurement> findAllByUser_Username(String username);

    boolean existsByIdAndUser_Id(Long id, Long userId);

    List<Measurement> findAllByUser_IdAndTypeAndDateBetweenOrderByDateAsc(Long userId,
            MeasurementType type, LocalDate startDate, LocalDate endDate);

    List<Measurement> findAllByUser_Id(Long userId);
    
}
