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
import com.hbelange.GymProgressTracker.exception.UserAlreadyExistsException;
import com.hbelange.GymProgressTracker.repository.UserRepository;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private String username;

    @BeforeEach
    void setUp() {
        username = "reg-" + UUID.randomUUID();
    }

    @Test
    void testRegisterUserReturnsIdAndUsername() {
        UserResponseDTO result = userService.registerUser(new UserRequestDTO(username, "plaintext"));

        assertNotNull(result.id());
        assertEquals(username, result.username());
    }

    @Test
    void testRegisterUserStoresPasswordEncoded() {
        userService.registerUser(new UserRequestDTO(username, "plaintext"));

        User saved = userRepository.findByUsername(username).orElseThrow();
        assertNotEquals("plaintext", saved.getPassword());
    }

    @Test
    @Transactional
    void testRegisterUserGrantsWriteAuthority() {
        userService.registerUser(new UserRequestDTO(username, "password"));

        User saved = userRepository.findByUsername(username).orElseThrow();
        assertEquals(1, saved.getAuthorities().size());
        assertEquals("write", saved.getAuthorities().get(0).getAuthority());
    }

    @Test
    void testRegisterUserWithDuplicateUsernameThrows() {
        userService.registerUser(new UserRequestDTO(username, "password"));

        assertThrows(UserAlreadyExistsException.class, () -> {
            userService.registerUser(new UserRequestDTO(username, "differentPassword"));
        });
    }

}
