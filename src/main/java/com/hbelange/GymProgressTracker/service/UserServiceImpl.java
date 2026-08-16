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
import org.springframework.transaction.annotation.Transactional;

import com.hbelange.GymProgressTracker.dto.AccountResponseDTO;
import com.hbelange.GymProgressTracker.dto.NewPasswordDTO;
import com.hbelange.GymProgressTracker.dto.UserRequestDTO;
import com.hbelange.GymProgressTracker.dto.UserResponseDTO;
import com.hbelange.GymProgressTracker.entity.Authority;
import com.hbelange.GymProgressTracker.entity.PasswordResetToken;
import com.hbelange.GymProgressTracker.entity.PendingEmailChange;
import com.hbelange.GymProgressTracker.entity.User;
import com.hbelange.GymProgressTracker.exception.DemoAccountRestrictedException;
import com.hbelange.GymProgressTracker.exception.UserAlreadyExistsException;
import com.hbelange.GymProgressTracker.repository.PasswordResetTokenRepository;
import com.hbelange.GymProgressTracker.repository.PendingEmailChangeRepository;
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
    private final PendingEmailChangeRepository pendingEmailChangeRepository;
    private final DemoAccountService demoAccountService;

    @Value("${jwt.secret}")
    private String secretKey;

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder().withoutPadding();

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService, PasswordResetTokenRepository tokenRepository, PendingEmailChangeRepository pendingEmailChangeRepository, DemoAccountService demoAccountService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.tokenRepository = tokenRepository;
        this.pendingEmailChangeRepository = pendingEmailChangeRepository;
        this.demoAccountService = demoAccountService;
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
        if (demoAccountService.isDemoAccount(user.getUsername())) {
            return; // Never let the shared demo account's password be reset
        }

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

    @Override
    public AccountResponseDTO getAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        PendingEmailChange pendingEmailChange = pendingEmailChangeRepository.findByUser(user);

        return new AccountResponseDTO(user.getUsername(), user.getEmail(), pendingEmailChange != null ? pendingEmailChange.getNewEmail() : null);
    }

    @Override
    public AccountResponseDTO updateUsername(Long userId, String newUsername) {
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (demoAccountService.isDemoAccount(user.getUsername())) {
            throw new DemoAccountRestrictedException("This is a shared demo account and can't be modified.");
        }

        if (userRepository.findByUsername(newUsername).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        user.setUsername(newUsername);
        userRepository.save(user);

        PendingEmailChange pendingEmailChange = pendingEmailChangeRepository.findByUser(user);

        return new AccountResponseDTO(user.getUsername(), user.getEmail(), pendingEmailChange != null ? pendingEmailChange.getNewEmail() : null);
    }

    @Override
    @Transactional
    public AccountResponseDTO changeEmail(Long userId, String newEmail) {
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (demoAccountService.isDemoAccount(user.getUsername())) {
            throw new DemoAccountRestrictedException("This is a shared demo account and can't be modified.");
        }

        // Check if the new email is already in use
        if (userRepository.findByEmail(newEmail).isPresent()) {
            throw new RuntimeException("Email already in use");
        }

        // Generate a token for email change verification
        String token = generateRandomToken();

        // before saving a new PendingEmailChange, delete any existing one for this user
        // flush so the delete hits the DB before the identity insert below, avoiding a unique constraint violation
        pendingEmailChangeRepository.deleteByUser_Id(user.getId());
        pendingEmailChangeRepository.flush();

        PendingEmailChange pendingEmailChange = new PendingEmailChange();
        pendingEmailChange.setNewEmail(newEmail);
        pendingEmailChange.setUser(user);
        pendingEmailChange.setToken(token);
        pendingEmailChange.setExpiresAt(LocalDateTime.now().plusMinutes(15)); // Token valid for 15 mins
        pendingEmailChangeRepository.save(pendingEmailChange);

        emailService.sendEmailChangeVerification(newEmail, token);

        return new AccountResponseDTO(user.getUsername(), user.getEmail(), newEmail);
    }

    @Override
    @Transactional
    public AccountResponseDTO cancelEmailChange(Long userId) {
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        pendingEmailChangeRepository.deleteByUser_Id(user.getId());

        return new AccountResponseDTO(user.getUsername(), user.getEmail(), null);
    }

    @Override
    public void handleEmailChangeVerification(String token) {
        
        PendingEmailChange pendingEmailChange = pendingEmailChangeRepository.findByToken(token);
        if (pendingEmailChange == null) {
            throw new RuntimeException("Invalid email change token");
        }

        if (pendingEmailChange.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Email change token has expired");
        }

        User user = pendingEmailChange.getUser();
        user.setEmail(pendingEmailChange.getNewEmail());
        userRepository.save(user);

        // Remove the pending email change after successful verification
        pendingEmailChangeRepository.delete(pendingEmailChange);
    }

    
}
