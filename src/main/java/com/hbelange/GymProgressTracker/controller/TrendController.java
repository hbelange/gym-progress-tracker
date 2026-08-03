package com.hbelange.GymProgressTracker.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import com.hbelange.GymProgressTracker.dto.ExerciseActivityDTO;
import com.hbelange.GymProgressTracker.dto.ExerciseTrendResponseDTO;
import com.hbelange.GymProgressTracker.dto.MeasurementTrendResponseDTO;
import com.hbelange.GymProgressTracker.dto.TrendRange;
import com.hbelange.GymProgressTracker.entity.MeasurementType;
import com.hbelange.GymProgressTracker.security.SecurityUser;
import com.hbelange.GymProgressTracker.service.TrendService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class TrendController {
    
    private final TrendService trendService;

    public TrendController(TrendService trendService) {
        this.trendService = trendService;
    }

    @GetMapping("/trend")
    public String trendRedirect() {
        return "redirect:/trend/measurements";
    }

    @GetMapping("/trend/measurements")
    public String measurementTrendPage(@RequestParam(defaultValue = "WEIGHT") String type,
                                        @RequestParam(defaultValue = "WEEK") String range,
                                        Model model) {
        model.addAttribute("type", MeasurementType.fromString(type));
        model.addAttribute("range", TrendRange.fromString(range));
        return "trendMeasurement";
    }

    @GetMapping("/trend/exercises")
    public String exerciseListPage() {
        return "trendExercises";
    }

    @GetMapping("/trend/exercises/{exerciseId}")
    public String exerciseTrendPage(@PathVariable Long exerciseId,
                                     @RequestParam(defaultValue = "WEEK") String range,
                                     Model model) {
        model.addAttribute("exerciseId", exerciseId);
        model.addAttribute("range", TrendRange.fromString(range));
        return "trendExercise";
    }

    @GetMapping("/api/trend/measurements")
    @ResponseBody
    public ResponseEntity<MeasurementTrendResponseDTO> getMeasurementTrend(
        @RequestParam(defaultValue = "WEIGHT") String type, 
        @RequestParam(defaultValue = "WEEK") String range,
        @AuthenticationPrincipal SecurityUser user
    ) {
        MeasurementType measurementType = MeasurementType.fromString(type);
        TrendRange trendRange = TrendRange.fromString(range);
        MeasurementTrendResponseDTO trend = trendService.getMeasurementTrend(user.getId(), measurementType, trendRange);
        return ResponseEntity.ok(trend);
    }

    @GetMapping("/api/trend/exercises")
    @ResponseBody
    public ResponseEntity<List<ExerciseActivityDTO>> getExercisesByRecentActivity(@AuthenticationPrincipal SecurityUser user) {
        List<ExerciseActivityDTO> exercises = trendService.getExercisesByRecentActivity(user.getId());
        return ResponseEntity.ok(exercises);
    }

    @GetMapping("/api/trend/exercises/{exerciseId}")
    @ResponseBody
    public ResponseEntity<ExerciseTrendResponseDTO> getExerciseTrend(
        @PathVariable Long exerciseId,
        @RequestParam(defaultValue = "WEEK") String range,
        @AuthenticationPrincipal SecurityUser user
    ) {
        TrendRange trendRange = TrendRange.fromString(range);
        ExerciseTrendResponseDTO trend = trendService.getExerciseTrend(user.getId(), exerciseId, trendRange);
        return ResponseEntity.ok(trend);
    }
    
    
}
