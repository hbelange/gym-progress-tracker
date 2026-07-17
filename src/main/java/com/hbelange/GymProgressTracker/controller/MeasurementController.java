package com.hbelange.GymProgressTracker.controller;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
        YearMonth yearMonth = (year != null && month != null) ? YearMonth.of(year, month) : YearMonth.now();

        List<LocalDate> days = new ArrayList<>();
        int leadingBlanks = yearMonth.atDay(1).getDayOfWeek().getValue() - 1;
        for (int i = 0; i < leadingBlanks; i++) {
            days.add(null);
        }
        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            days.add(yearMonth.atDay(day));
        }
        while (days.size() % 7 != 0) {
            days.add(null);
        }

        List<List<LocalDate>> weeks = new ArrayList<>();
        for (int i = 0; i < days.size(); i += 7) {
            weeks.add(days.subList(i, i + 7));
        }

        YearMonth previousMonth = yearMonth.minusMonths(1);
        YearMonth nextMonth = yearMonth.plusMonths(1);

        model.addAttribute("weeks", weeks);
        model.addAttribute("monthLabel", yearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + yearMonth.getYear());
        model.addAttribute("prevYear", previousMonth.getYear());
        model.addAttribute("prevMonth", previousMonth.getMonthValue());
        model.addAttribute("nextYear", nextMonth.getYear());
        model.addAttribute("nextMonth", nextMonth.getMonthValue());
        model.addAttribute("today", LocalDate.now());

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
        List<MeasurementResponseDTO> measurements = measurementService.getMeasurements(authentication.getName());
        return ResponseEntity.ok(measurements);
    }   

    @PostMapping("/api/measurement")
    @ResponseBody
    public ResponseEntity<MeasurementResponseDTO> createMeasurement(@Valid @RequestBody MeasurementRequestDTO measurementRequestDTO, Authentication authentication) {
        MeasurementResponseDTO createdMeasurement = measurementService.createMeasurement(measurementRequestDTO, authentication.getName());
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
