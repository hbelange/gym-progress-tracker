package com.hbelange.GymProgressTracker.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hbelange.GymProgressTracker.entity.Authority;
import com.hbelange.GymProgressTracker.entity.Exercise;
import com.hbelange.GymProgressTracker.entity.Measurement;
import com.hbelange.GymProgressTracker.entity.MeasurementType;
import com.hbelange.GymProgressTracker.entity.Set;
import com.hbelange.GymProgressTracker.entity.User;
import com.hbelange.GymProgressTracker.entity.Workout;
import com.hbelange.GymProgressTracker.entity.WorkoutType;
import com.hbelange.GymProgressTracker.repository.ExerciseRepository;
import com.hbelange.GymProgressTracker.repository.MeasurementRepository;
import com.hbelange.GymProgressTracker.repository.SetRepository;
import com.hbelange.GymProgressTracker.repository.UserRepository;
import com.hbelange.GymProgressTracker.repository.WorkoutRepository;
import com.hbelange.GymProgressTracker.repository.WorkoutTypeRepository;

@Service
public class DemoDataSeederImpl implements DemoDataSeeder {

    private static final int DAYS = 28;

    private final DemoAccountService demoAccountService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final WorkoutTypeRepository workoutTypeRepository;
    private final ExerciseRepository exerciseRepository;
    private final WorkoutRepository workoutRepository;
    private final SetRepository setRepository;
    private final MeasurementRepository measurementRepository;

    public DemoDataSeederImpl(DemoAccountService demoAccountService, UserRepository userRepository,
            PasswordEncoder passwordEncoder, WorkoutTypeRepository workoutTypeRepository,
            ExerciseRepository exerciseRepository, WorkoutRepository workoutRepository,
            SetRepository setRepository, MeasurementRepository measurementRepository) {
        this.demoAccountService = demoAccountService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.workoutTypeRepository = workoutTypeRepository;
        this.exerciseRepository = exerciseRepository;
        this.workoutRepository = workoutRepository;
        this.setRepository = setRepository;
        this.measurementRepository = measurementRepository;
    }

    @Override
    @Transactional
    public void reseed() {
        User demoUser = findOrCreateDemoUser();
        clearExistingData(demoUser);
        seedMeasurements(demoUser);
        seedWorkouts(demoUser);
    }

    private User findOrCreateDemoUser() {
        return userRepository.findByUsername(demoAccountService.getDemoUsername())
                .orElseGet(this::createDemoUser);
    }

    private User createDemoUser() {
        User user = new User();
        user.setUsername(demoAccountService.getDemoUsername());
        user.setEmail(demoAccountService.getDemoUsername() + "@example.com");
        user.setPassword(passwordEncoder.encode(demoAccountService.getDemoPassword()));
        user.setEnabled(1);

        Authority authority = new Authority();
        authority.setUser(user);
        authority.setAuthority("write");
        user.setAuthorities(List.of(authority));

        return userRepository.save(user);
    }

    private void clearExistingData(User demoUser) {
        // flush after each delete so it hits the DB before the identity inserts below,
        // avoiding unique constraint violations (workout_type/exercise names are unique per user)
        setRepository.deleteAllByUser_Id(demoUser.getId());
        setRepository.flush();
        workoutRepository.deleteAllByUser_Id(demoUser.getId());
        workoutRepository.flush();
        exerciseRepository.deleteAllByUser_Id(demoUser.getId());
        exerciseRepository.flush();
        workoutTypeRepository.deleteAllByUser_Id(demoUser.getId());
        workoutTypeRepository.flush();
        measurementRepository.deleteAllByUser_Id(demoUser.getId());
        measurementRepository.flush();
    }

    private void seedMeasurements(User demoUser) {
        for (int daysAgo = DAYS - 1; daysAgo >= 0; daysAgo--) {
            LocalDate date = LocalDate.now().minusDays(daysAgo);
            int weeksAgo = daysAgo / 7;
            double weight = 185.0 - (3 - weeksAgo) * 1.0;
            double steps = 6000 + (daysAgo % 7) * 300;
            double calories = 2200 - weeksAgo * 50;

            saveMeasurement(demoUser, date, MeasurementType.WEIGHT, weight);
            saveMeasurement(demoUser, date, MeasurementType.STEPS, steps);
            saveMeasurement(demoUser, date, MeasurementType.CALORIES, calories);
        }
    }

    private void saveMeasurement(User demoUser, LocalDate date, MeasurementType type, double value) {
        Measurement measurement = new Measurement();
        measurement.setUser(demoUser);
        measurement.setDate(date);
        measurement.setType(type);
        measurement.setValue(value);
        measurementRepository.save(measurement);
    }

    private void seedWorkouts(User demoUser) {
        WorkoutType workoutType = new WorkoutType();
        workoutType.setName("Demo - Full Body");
        workoutType.setUser(demoUser);
        workoutType = workoutTypeRepository.save(workoutType);

        Exercise bench = saveExercise(demoUser, "Demo - Bench Press");
        Exercise squat = saveExercise(demoUser, "Demo - Squat");
        Exercise deadlift = saveExercise(demoUser, "Demo - Deadlift");

        for (int daysAgo = DAYS - 1; daysAgo >= 0; daysAgo -= 2) {
            LocalDate date = LocalDate.now().minusDays(daysAgo);
            int weeksAgo = daysAgo / 7;
            int progress = 3 - weeksAgo;

            Workout workout = new Workout();
            workout.setDate(date);
            workout.setUser(demoUser);
            workout.setWorkoutType(workoutType);
            workout = workoutRepository.save(workout);

            addSets(demoUser, workout, bench, 135.0 + progress * 2.5);
            addSets(demoUser, workout, squat, 185.0 + progress * 5.0);
            addSets(demoUser, workout, deadlift, 225.0 + progress * 5.0);
        }
    }

    private Exercise saveExercise(User demoUser, String name) {
        Exercise exercise = new Exercise();
        exercise.setName(name);
        exercise.setUser(demoUser);
        return exerciseRepository.save(exercise);
    }

    private void addSets(User demoUser, Workout workout, Exercise exercise, double weight) {
        for (int setNumber = 1; setNumber <= 3; setNumber++) {
            Set set = new Set();
            set.setWorkout(workout);
            set.setExercise(exercise);
            set.setUser(demoUser);
            set.setReps(8 - setNumber);
            set.setRepsInReserve(2);
            set.setWeight(weight);
            set.setSetNumber(setNumber);
            setRepository.save(set);
        }
    }

}
