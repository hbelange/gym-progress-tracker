package com.hbelange.GymProgressTracker.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.hbelange.GymProgressTracker.dto.NewPasswordDTO;
import com.hbelange.GymProgressTracker.dto.UserRequestDTO;
import com.hbelange.GymProgressTracker.dto.UserResponseDTO;
import com.hbelange.GymProgressTracker.entity.Authority;
import com.hbelange.GymProgressTracker.entity.PasswordResetToken;
import com.hbelange.GymProgressTracker.entity.User;
import com.hbelange.GymProgressTracker.exception.UserAlreadyExistsException;
import com.hbelange.GymProgressTracker.repository.PasswordResetTokenRepository;
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
    private final PasswordResetTokenRepository tokenRepository;

    @Value("${jwt.secret}")
    private String secretKey;

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder().withoutPadding();

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService, PasswordResetTokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.tokenRepository = tokenRepository;
    }

    private String generateRandomToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
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
    public void validatePasswordResetToken(String token) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid password reset token"));
        if (resetToken.isExpired()) {
            throw new RuntimeException("Password reset token has expired");
        }
    }

    @Override
    public void resendVerificationEmail(String username) {
        userRepository.findByUsername(username)
                .filter(user -> user.getEnabled() == 0)
                .ifPresent(emailService::sendVerificationEmail);
    }

    @Override
    public void handlePasswordReset(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()){
            return; // If user with the given email doesn't exist, do nothing
        }

        User user = userOpt.get();
        String token = generateRandomToken();

        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setToken(token);
        passwordResetToken.setUser(user);
        passwordResetToken.setExpiresAt(LocalDateTime.now().plusMinutes(30)); // Token valid for 30 mins

        tokenRepository.save(passwordResetToken);

        emailService.sendPasswordResetEmail(user, token);
    }

    @Override
    public void resetPassword(NewPasswordDTO newPasswordDTO) {
        if (!newPasswordDTO.password().equals(newPasswordDTO.confirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        validatePasswordResetToken(newPasswordDTO.token());

        PasswordResetToken resetToken = tokenRepository.findByToken(newPasswordDTO.token())
                .orElseThrow(() -> new RuntimeException("Invalid password reset token"));

        // Update the user's password
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPasswordDTO.password()));
        userRepository.save(user);

        // Invalidate the token after use
        tokenRepository.delete(resetToken);
        
    }
}
