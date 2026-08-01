package com.hbelange.GymProgressTracker.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.hbelange.GymProgressTracker.dto.UserRequestDTO;
import com.hbelange.GymProgressTracker.dto.UserResponseDTO;
import com.hbelange.GymProgressTracker.entity.Authority;
import com.hbelange.GymProgressTracker.entity.User;
import com.hbelange.GymProgressTracker.exception.UserAlreadyExistsException;
import com.hbelange.GymProgressTracker.repository.UserRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${jwt.secret}")
    private String secretKey;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
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
        newUser.setEmail(userRequestDTO.email());
        newUser.setPassword(passwordEncoder.encode(password));

        Authority authority = new Authority();
        authority.setUser(newUser);
        authority.setAuthority("write");
        newUser.setAuthorities(List.of(authority));

        newUser = userRepository.save(newUser);
        emailService.sendVerificationEmail(newUser);
        return new UserResponseDTO(newUser.getId(), newUser.getUsername(), newUser.getEmail());
    }

    @Override
    public void handleVerification(String token) {
        // Implement the logic to handle email verification here
        // This would typically involve parsing the token, validating it, and updating the user's status in the database
        Claims claims;
        try {
            byte[] keyBytes = Decoders.BASE64.decode(secretKey); // Use the same secret key used for signing the token
            SecretKey key = Keys.hmacShaKeyFor(keyBytes);

            claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("Token has expired");
        } catch (JwtException e) {
            throw new RuntimeException("Invalid token");
        }

        String username = claims.getSubject();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getEnabled() == 1) {
            throw new RuntimeException("User already verified");
        }

        user.setEnabled(1);
        userRepository.save(user);
    }

    @Override
    public void resendVerificationEmail(String username) {
        userRepository.findByUsername(username)
                .filter(user -> user.getEnabled() == 0)
                .ifPresent(emailService::sendVerificationEmail);
    }
}
