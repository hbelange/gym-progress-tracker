package com.hbelange.GymProgressTracker.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.hbelange.GymProgressTracker.dto.SetResponseDTO;
import com.hbelange.GymProgressTracker.dto.WorkoutRequestDTO;
import com.hbelange.GymProgressTracker.dto.WorkoutResponseDTO;
import com.hbelange.GymProgressTracker.security.SecurityUser;
import com.hbelange.GymProgressTracker.service.WorkoutService;
import com.hbelange.GymProgressTracker.web.MonthCalendarView;

import jakarta.validation.Valid;

@Controller
public class WorkoutController {

    private final WorkoutService workoutService;

    public WorkoutController(WorkoutService workoutService) {
        this.workoutService = workoutService;
    }

    @GetMapping("/workout")
    public String workoutPage(@RequestParam(required = false) Integer year,
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
        return "workout";
    }

    @GetMapping("/workout/{date}")
    public String workoutDayPage(@PathVariable LocalDate date, Model model) {
        model.addAttribute("date", date);
        return "workoutDay";
    }

    @GetMapping("/api/workouts")
    @ResponseBody
    public ResponseEntity<List<WorkoutResponseDTO>> getAllWorkouts(@AuthenticationPrincipal SecurityUser user) {
        List<WorkoutResponseDTO> workouts = workoutService.getAllWorkouts(user.getId());
        return ResponseEntity.ok(workouts);
    }

    @PostMapping("/api/workouts")
    @ResponseBody
    public ResponseEntity<WorkoutResponseDTO> createWorkout(@Valid @RequestBody WorkoutRequestDTO workoutRequestDTO, @AuthenticationPrincipal SecurityUser user) {
        WorkoutResponseDTO workout = workoutService.createWorkout(workoutRequestDTO, user.getId());
        return ResponseEntity.ok(workout);
    }

    @GetMapping("/api/workouts/{workoutId}")
    @ResponseBody
    public ResponseEntity<WorkoutResponseDTO> getWorkoutById(@PathVariable Long workoutId) {
        WorkoutResponseDTO workout = workoutService.getWorkoutById(workoutId);
        return ResponseEntity.ok(workout);
    }

    @PutMapping("/api/workouts/{workoutId}")
    @ResponseBody
    public ResponseEntity<WorkoutResponseDTO> updateWorkout(@PathVariable Long workoutId, @Valid @RequestBody WorkoutRequestDTO workoutRequestDTO) {
        WorkoutResponseDTO workout = workoutService.updateWorkout(workoutId, workoutRequestDTO);
        return ResponseEntity.ok(workout);
    }

    @DeleteMapping("/api/workouts/{workoutId}")
    @ResponseBody
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWorkout(@PathVariable Long workoutId) {
        workoutService.deleteWorkout(workoutId);
    }

    @GetMapping("/api/workouts/{workoutId}/sets")
    @ResponseBody
    public ResponseEntity<List<SetResponseDTO>> getSetsByWorkoutId(@PathVariable Long workoutId) {
        List<SetResponseDTO> sets = workoutService.getSetsByWorkoutId(workoutId);
        return ResponseEntity.ok(sets);
    }

}
