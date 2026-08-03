package com.hbelange.GymProgressTracker.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.hbelange.GymProgressTracker.dto.MeasurementRequestDTO;
import com.hbelange.GymProgressTracker.dto.MeasurementResponseDTO;
import com.hbelange.GymProgressTracker.service.MeasurementService;
import com.hbelange.GymProgressTracker.web.MonthCalendarView;

import org.springframework.http.HttpStatus;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class MeasurementController {

    private final MeasurementService measurementService;

    public MeasurementController(MeasurementService measurementService) {
        this.measurementService = measurementService;
    }

    @GetMapping("/measurement")
    public String measurementPage(@RequestParam(required = false) Integer year,
                                   @RequestParam(required = false) Integer month,
                                   Model model) {
        MonthCalendarView calendar = MonthCalendarView.of(year, month);
        model.addAttribute("weeks", calendar.weeks());
        model.addAttribute("monthLabel", calendar.monthLabel());
        model.addAttribute("prevYear", calendar.prevYear());
        model.addAttribute("prevMonth", calendar.prevMonth());
        model.addAttribute("nextYear", calendar.nextYear());
        model.addAttribute("nextMonth", calendar.nextMonth());
        model.addAttribute("today", calendar.today());
        return "measurement";
    }

    @GetMapping("/measurement/{date}")
    public String measurementDayPage(@PathVariable LocalDate date, Model model) {
        model.addAttribute("date", date);
        return "measurementDay";
    }


    @GetMapping("/api/measurement")
    @ResponseBody
    public ResponseEntity<List<MeasurementResponseDTO>> getMeasurements(Authentication authentication) {
        List<MeasurementResponseDTO> measurements = measurementService.getMeasurements(Long.valueOf(authentication.getName()));
        return ResponseEntity.ok(measurements);
    }   

    @PostMapping("/api/measurement")
    @ResponseBody
    public ResponseEntity<MeasurementResponseDTO> createMeasurement(@Valid @RequestBody MeasurementRequestDTO measurementRequestDTO, Authentication authentication) {
        MeasurementResponseDTO createdMeasurement = measurementService.createMeasurement(measurementRequestDTO, Long.valueOf(authentication.getName()));
        return ResponseEntity.status(201).body(createdMeasurement);
    }

    @PutMapping("/api/measurement/{measurementId}")
    @ResponseBody
    public ResponseEntity<MeasurementResponseDTO> updateMeasurement(@PathVariable Long measurementId, @Valid @RequestBody MeasurementRequestDTO measurementRequestDTO) {
        MeasurementResponseDTO updatedMeasurement = measurementService.updateMeasurement(measurementId, measurementRequestDTO);
        return ResponseEntity.ok(updatedMeasurement);
    }

    @DeleteMapping("/api/measurement/{measurementId}")
    @ResponseBody
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMeasurement(@PathVariable Long measurementId) {
        measurementService.deleteMeasurement(measurementId);
    }

}
