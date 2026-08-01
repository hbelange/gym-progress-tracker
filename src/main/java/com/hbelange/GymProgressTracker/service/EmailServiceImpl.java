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

    @Value("${jwt.secret}")
    private String secretKey;

    private final JavaMailSender mailSender;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    @Async
    public void sendVerificationEmail(User user) {
        String token = createVerificationToken(user.getUsername());
        String verificationLink = "https://localhost:8080/verify?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("harrisonbelanger@gmail.com"); // TODO: Replace with user.getEmail() in production
        message.setSubject("Verify your email: " + user.getEmail());
        message.setText("Please click the following link to verify your email: " + verificationLink);
        mailSender.send(message);
    }

    private String createVerificationToken(String username) {
        Instant now = Instant.now();
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);    // 256-bit Base64 encoded secret key
    SecretKey key = Keys.hmacShaKeyFor(keyBytes);
        
        return Jwts.builder()
                .subject(username)
                .claim("email", "harrisonbelanger@gmail.com")
                .claim("purpose", "email_verification")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(900))) // Token valid for 15 mins
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }
    
}
