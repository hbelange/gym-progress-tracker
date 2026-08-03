package com.hbelange.GymProgressTracker.repository;
import com.hbelange.GymProgressTracker.entity.PendingEmailChange;
import com.hbelange.GymProgressTracker.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PendingEmailChangeRepository extends JpaRepository<PendingEmailChange, Long> {
    PendingEmailChange findByToken(String token);
    PendingEmailChange findByUser(User user);
    void deleteByUser_Id(Long userId);
    
}
