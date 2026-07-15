package com.hbelange.GymProgressTracker.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.hbelange.GymProgressTracker.dto.UserRequestDTO;
import com.hbelange.GymProgressTracker.dto.UserResponseDTO;
import com.hbelange.GymProgressTracker.entity.Authority;
import com.hbelange.GymProgressTracker.entity.User;
import com.hbelange.GymProgressTracker.exception.UserAlreadyExistsException;
import com.hbelange.GymProgressTracker.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserResponseDTO registerUser(UserRequestDTO userRequestDTO) {
        // Implement the logic to register a user here
        String username = userRequestDTO.username();
        String password = userRequestDTO.password();

        if (userRepository.findByUsername(username).isPresent()) {
            throw new UserAlreadyExistsException("Username already exists: " + username);
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password));

        Authority authority = new Authority();
        authority.setUser(newUser);
        authority.setAuthority("write");
        newUser.setAuthorities(List.of(authority));

        newUser = userRepository.save(newUser);
        return new UserResponseDTO(newUser.getId(), newUser.getUsername());
    }
}
