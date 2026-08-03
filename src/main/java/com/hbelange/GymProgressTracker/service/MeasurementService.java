package com.hbelange.GymProgressTracker.service;

import java.util.List;

import com.hbelange.GymProgressTracker.dto.MeasurementRequestDTO;
import com.hbelange.GymProgressTracker.dto.MeasurementResponseDTO;

public interface MeasurementService {

    public MeasurementResponseDTO createMeasurement(MeasurementRequestDTO measurementRequestDTO, Long userId);

    public List<MeasurementResponseDTO> getMeasurements(Long userId);

    public MeasurementResponseDTO updateMeasurement(Long measurementId, MeasurementRequestDTO measurementRequestDTO);
    
    public void deleteMeasurement(Long measurementId);
    
}
