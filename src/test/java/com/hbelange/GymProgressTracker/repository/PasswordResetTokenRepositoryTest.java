package com.hbelange.GymProgressTracker.repository;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import com.hbelange.GymProgressTracker.TestcontainersConfiguration;
import com.hbelange.GymProgressTracker.entity.PasswordResetToken;
import com.hbelange.GymProgressTracker.entity.User;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class PasswordResetTokenRepositoryTest {

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void savedTokenIsPersistedToTheFlywayManagedTable() {
        String username = "pwd-reset-repo-test-" + UUID.randomUUID();
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword("password");
        user.setEnabled(1);
        user.setAuthorities(List.of());
        user = userRepository.save(user);

        PasswordResetToken token = new PasswordResetToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        passwordResetTokenRepository.saveAndFlush(token);

        assertTrue(passwordResetTokenRepository.findByToken(token.getToken()).isPresent());
    }

}
