package com.hbelange.GymProgressTracker.service;

import java.util.List;

import com.hbelange.GymProgressTracker.dto.MeasurementRequestDTO;
import com.hbelange.GymProgressTracker.dto.MeasurementResponseDTO;

public interface MeasurementService {

    public MeasurementResponseDTO createMeasurement(MeasurementRequestDTO measurementRequestDTO, String username);

    public List<MeasurementResponseDTO> getMeasurements(String username);

    public MeasurementResponseDTO updateMeasurement(Long measurementId, MeasurementRequestDTO measurementRequestDTO);
    
    public void deleteMeasurement(Long measurementId);
    
}
