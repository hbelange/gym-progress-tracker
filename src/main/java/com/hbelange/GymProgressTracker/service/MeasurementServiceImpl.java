package com.hbelange.GymProgressTracker.service;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.hbelange.GymProgressTracker.dto.MeasurementRequestDTO;
import com.hbelange.GymProgressTracker.dto.MeasurementResponseDTO;
import com.hbelange.GymProgressTracker.entity.Measurement;
import com.hbelange.GymProgressTracker.entity.User;
import com.hbelange.GymProgressTracker.repository.MeasurementRepository;
import com.hbelange.GymProgressTracker.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class MeasurementServiceImpl implements MeasurementService {
    
    private final MeasurementRepository measurementRepository;
    private final UserRepository userRepository;

    public MeasurementServiceImpl(MeasurementRepository measurementRepository, UserRepository userRepository) {
        this.measurementRepository = measurementRepository;
        this.userRepository = userRepository;
    }

    @Override
    public MeasurementResponseDTO createMeasurement(MeasurementRequestDTO measurementRequestDTO, Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        
        Measurement measurement = new Measurement();
        measurement.setDate(measurementRequestDTO.date());
        measurement.setType(measurementRequestDTO.measurementType());
        measurement.setValue(measurementRequestDTO.value());
        measurement.setUser(user);
        measurementRepository.save(measurement);
        return new MeasurementResponseDTO(measurement.getId(), measurement.getDate(), measurement.getType(), measurement.getValue());
    }

    @Override
    public List<MeasurementResponseDTO> getMeasurements(Long userId) {
        List<Measurement> measurements = measurementRepository.findAllByUser_Id(userId);
        return measurements.stream()
                .map(measurement -> new MeasurementResponseDTO(measurement.getId(), measurement.getDate(), measurement.getType(), measurement.getValue()))
                .toList();
    }

    @Override
    @PreAuthorize("@measurementRepository.existsByIdAndUser_Id(#measurementId, authentication.name)")
    public MeasurementResponseDTO updateMeasurement(Long measurementId, MeasurementRequestDTO measurementRequestDTO) {
        Measurement measurement = measurementRepository.findById(measurementId)
            .orElseThrow(() -> new EntityNotFoundException("Measurement not found with id: " + measurementId));

        measurement.setDate(measurementRequestDTO.date());
        measurement.setType(measurementRequestDTO.measurementType());
        measurement.setValue(measurementRequestDTO.value());
        measurementRepository.save(measurement);
        return new MeasurementResponseDTO(measurement.getId(), measurement.getDate(), measurement.getType(), measurement.getValue());
    }

    @Override
    @PreAuthorize("@measurementRepository.existsByIdAndUser_Id(#measurementId, authentication.name)")
    public void deleteMeasurement(Long measurementId) {
        measurementRepository.deleteById(measurementId);
    }

}
