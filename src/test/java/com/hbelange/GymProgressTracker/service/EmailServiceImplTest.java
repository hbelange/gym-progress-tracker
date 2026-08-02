package com.hbelange.GymProgressTracker.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.hbelange.GymProgressTracker.entity.User;

class EmailServiceImplTest {

    private static final String TEST_SECRET = "CenQebb4t7t8iux1hCXjDJ3BxlylNky7vaRvM6y/Jqk=";
    private static final String BASE_URL = "https://ec2-54-91-45-98.compute-1.amazonaws.com:8080";

    private final JavaMailSender mailSender = mock(JavaMailSender.class);
    private final EmailServiceImpl emailService = new EmailServiceImpl(mailSender, TEST_SECRET, BASE_URL);

    private User userWithEmail(String email) {
        User user = new User();
        user.setUsername("someuser");
        user.setEmail(email);
        return user;
    }

    @Test
    void sendVerificationEmailSendsToTheUsersOwnAddress() {
        emailService.sendVerificationEmail(userWithEmail("real-user@example.com"));

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertTrue(Arrays.asList(captor.getValue().getTo()).contains("real-user@example.com"));
    }

    @Test
    void sendVerificationEmailLinkUsesConfiguredBaseUrl() {
        emailService.sendVerificationEmail(userWithEmail("real-user@example.com"));

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertTrue(captor.getValue().getText().contains(BASE_URL + "/verify?token="));
    }

    @Test
    void sendPasswordResetEmailSendsToTheUsersOwnAddress() {
        emailService.sendPasswordResetEmail(userWithEmail("real-user@example.com"), "some-token");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertTrue(Arrays.asList(captor.getValue().getTo()).contains("real-user@example.com"));
    }

    @Test
    void sendPasswordResetEmailLinkUsesConfiguredBaseUrlAndToken() {
        emailService.sendPasswordResetEmail(userWithEmail("real-user@example.com"), "some-token");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertTrue(captor.getValue().getText().contains(BASE_URL + "/reset-password?token=some-token"));
    }
}
