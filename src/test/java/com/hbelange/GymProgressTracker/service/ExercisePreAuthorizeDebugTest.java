package com.hbelange.GymProgressTracker.service;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.hbelange.GymProgressTracker.dto.ExerciseRequestDTO;

@SpringBootTest
class ExercisePreAuthorizeDebugTest {

    @Autowired
    ExerciseService exerciseService;

    @Test
    void updateAsOwner() {
        var auth = new UsernamePasswordAuthenticationToken(
                "harrison", "password", List.of(new SimpleGrantedAuthority("write")));
        System.out.println("authentication.getName() = " + auth.getName());
        SecurityContextHolder.getContext().setAuthentication(auth);
        try {
            var result = exerciseService.updateExercise(17L, new ExerciseRequestDTO("Machine Crunch"));
            System.out.println("SUCCESS: " + result);
        } catch (Exception e) {
            System.out.println("EXCEPTION: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
