package com.hbelange.GymProgressTracker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import com.hbelange.GymProgressTracker.TestcontainersConfiguration;
import com.hbelange.GymProgressTracker.entity.User;
import com.hbelange.GymProgressTracker.repository.ExerciseRepository;
import com.hbelange.GymProgressTracker.repository.MeasurementRepository;
import com.hbelange.GymProgressTracker.repository.SetRepository;
import com.hbelange.GymProgressTracker.repository.UserRepository;
import com.hbelange.GymProgressTracker.repository.WorkoutRepository;
import com.hbelange.GymProgressTracker.repository.WorkoutTypeRepository;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class DemoDataSeederTest {

    @Autowired
    private DemoDataSeeder demoDataSeeder;

    @Autowired
    private DemoAccountService demoAccountService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkoutRepository workoutRepository;

    @Autowired
    private SetRepository setRepository;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private WorkoutTypeRepository workoutTypeRepository;

    @Autowired
    private MeasurementRepository measurementRepository;

    @Test
    void reseedCreatesDemoUserIfMissing() {
        demoDataSeeder.reseed();

        User demoUser = userRepository.findByUsername(demoAccountService.getDemoUsername()).orElseThrow();
        assertTrue(demoUser.getEnabled() == 1);
    }

    @Test
    void reseedIsIdempotentAndDoesNotDuplicateData() {
        demoDataSeeder.reseed();
        Long demoUserId = userRepository.findByUsername(demoAccountService.getDemoUsername()).orElseThrow().getId();
        long workoutsAfterFirstReseed = workoutRepository.findAllByUser_Id(demoUserId).size();
        long measurementsAfterFirstReseed = measurementRepository.findAllByUser_Id(demoUserId).size();

        demoDataSeeder.reseed();
        long workoutsAfterSecondReseed = workoutRepository.findAllByUser_Id(demoUserId).size();
        long measurementsAfterSecondReseed = measurementRepository.findAllByUser_Id(demoUserId).size();

        assertTrue(workoutsAfterFirstReseed > 0);
        assertTrue(measurementsAfterFirstReseed > 0);
        assertEquals(workoutsAfterFirstReseed, workoutsAfterSecondReseed);
        assertEquals(measurementsAfterFirstReseed, measurementsAfterSecondReseed);
    }

}
