package com.hbelange.GymProgressTracker.config;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

@Configuration
@EnableMethodSecurity
public class WebAuthorizationConfig {

    @Bean
    @Order(3)
    SecurityFilterChain configure(HttpSecurity http) throws Exception {

        return http
            .formLogin(form -> form
                .loginPage("/login")
                .failureHandler(loginFailureHandler())
                .permitAll())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/error", "/register", "/register/pending", "/login", "/css/**", "/verify", "/resend-verification", "/forgot-password", "/reset-password").permitAll()
                .anyRequest().authenticated())
            .build();
    }

    private AuthenticationFailureHandler loginFailureHandler() {
        return (request, response, exception) -> {
            if (exception instanceof DisabledException) {
                String username = URLEncoder.encode(request.getParameter("username"), StandardCharsets.UTF_8);
                response.sendRedirect("/login?unverified&username=" + username);
            } else {
                response.sendRedirect("/login?error");
            }
        };
    }
}
