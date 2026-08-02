package com.hbelange.GymProgressTracker.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class PasswordResetToken {
    
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Token cannot be blank")
    private String token;

    @NotNull(message = "Token must expire")
    private LocalDateTime expiresAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
