package com.hbelange.GymProgressTracker.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.hbelange.GymProgressTracker.entity.User;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final String secretKey;
    private final String baseUrl;

    public EmailServiceImpl(
            JavaMailSender mailSender,
            @Value("${jwt.secret}") String secretKey,
            @Value("${app.base-url}") String baseUrl) {
        this.mailSender = mailSender;
        this.secretKey = secretKey;
        this.baseUrl = baseUrl;
    }

    @Override
    @Async
    public void sendVerificationEmail(User user) {
        String token = createVerificationToken(user.getUsername());
        String verificationLink = baseUrl + "/verify?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Verify your email");
        message.setText("Please click the following link to verify your email: " + verificationLink);
        mailSender.send(message);
    }

    private String createVerificationToken(String username) {
        Instant now = Instant.now();
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);

        return Jwts.builder()
                .subject(username)
                .claim("purpose", "email_verification")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(900))) // Token valid for 15 mins
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    @Override
    @Async
    public void sendPasswordResetEmail(User user, String token) {
        String resetLink = baseUrl + "/reset-password?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
        String body = "Click the following link to reset your password: " + resetLink + "\n\nThis link will expire in 30 minutes.";
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Reset your password");
        message.setText(body);
        mailSender.send(message);
    }

    @Override
    @Async
    public void sendEmailChangeVerification(String newEmail, String token) {
        String changeLink = baseUrl + "/change-email?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
        String body = "Click the following link to change your email: " + changeLink + "\n\nThis link will expire in 15 minutes.";
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(newEmail);
        message.setSubject("Change your email");
        message.setText(body);
        mailSender.send(message);
    }

}
