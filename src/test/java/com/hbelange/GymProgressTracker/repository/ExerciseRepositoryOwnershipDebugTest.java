package com.hbelange.GymProgressTracker.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ExerciseRepositoryOwnershipDebugTest {

    @Autowired
    ExerciseRepository exerciseRepository;

    @Test
    void checkOwnership() {
        boolean exists = exerciseRepository.existsByIdAndUser_Username(17L, "harrison");
        System.out.println("RESULT existsByIdAndUser_Username(17, harrison) = " + exists);
    }
}
