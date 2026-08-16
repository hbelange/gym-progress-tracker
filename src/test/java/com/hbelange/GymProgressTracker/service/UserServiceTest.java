package com.hbelange.GymProgressTracker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import com.hbelange.GymProgressTracker.TestcontainersConfiguration;
import com.hbelange.GymProgressTracker.dto.UserRequestDTO;
import com.hbelange.GymProgressTracker.dto.UserResponseDTO;
import com.hbelange.GymProgressTracker.entity.User;
import com.hbelange.GymProgressTracker.exception.DemoAccountRestrictedException;
import com.hbelange.GymProgressTracker.exception.UserAlreadyExistsException;
import com.hbelange.GymProgressTracker.repository.PasswordResetTokenRepository;
import com.hbelange.GymProgressTracker.repository.UserRepository;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DemoAccountService demoAccountService;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    private String username;
    private String email;

    @BeforeEach
    void setUp() {
        username = "reg-" + UUID.randomUUID();
        email = username + "@example.com";
    }

    private User findOrCreateDemoUser() {
        return userRepository.findByUsername(demoAccountService.getDemoUsername())
                .orElseGet(() -> {
                    User demoUser = new User();
                    demoUser.setUsername(demoAccountService.getDemoUsername());
                    demoUser.setEmail(demoAccountService.getDemoUsername() + "@example.com");
                    demoUser.setPassword("password");
                    demoUser.setEnabled(1);
                    demoUser.setAuthorities(java.util.List.of());
                    return userRepository.save(demoUser);
                });
    }

    @Test
    void testRegisterUserReturnsIdAndUsername() {
        UserResponseDTO result = userService.registerUser(new UserRequestDTO(email, username, "plaintext"));

        assertNotNull(result.id());
        assertEquals(username, result.username());
    }

    @Test
    void testRegisterUserStoresPasswordEncoded() {
        userService.registerUser(new UserRequestDTO(email, username, "plaintext"));

        User saved = userRepository.findByUsername(username).orElseThrow();
        assertNotEquals("plaintext", saved.getPassword());
    }

    @Test
    @Transactional
    void testRegisterUserGrantsWriteAuthority() {
        userService.registerUser(new UserRequestDTO(email, username, "password"));

        User saved = userRepository.findByUsername(username).orElseThrow();
        assertEquals(1, saved.getAuthorities().size());
        assertEquals("write", saved.getAuthorities().get(0).getAuthority());
    }

    @Test
    void testRegisterUserWithDuplicateUsernameThrows() {
        userService.registerUser(new UserRequestDTO(email, username, "password"));

        assertThrows(UserAlreadyExistsException.class, () -> {
            userService.registerUser(new UserRequestDTO("different-" + email, username, "differentPassword"));
        });
    }

    @Test
    void testUpdateUsernameOnDemoAccountThrows() {
        User demoUser = findOrCreateDemoUser();

        assertThrows(DemoAccountRestrictedException.class, () -> {
            userService.updateUsername(demoUser.getId(), "hacker-" + UUID.randomUUID());
        });
    }

    @Test
    void testChangeEmailOnDemoAccountThrows() {
        User demoUser = findOrCreateDemoUser();

        assertThrows(DemoAccountRestrictedException.class, () -> {
            userService.changeEmail(demoUser.getId(), "hacker-" + UUID.randomUUID() + "@example.com");
        });
    }

    @Test
    void testHandlePasswordResetForDemoAccountDoesNotCreateToken() {
        User demoUser = findOrCreateDemoUser();
        long tokensBefore = passwordResetTokenRepository.count();

        userService.handlePasswordReset(demoUser.getEmail());

        assertEquals(tokensBefore, passwordResetTokenRepository.count());
    }

}
