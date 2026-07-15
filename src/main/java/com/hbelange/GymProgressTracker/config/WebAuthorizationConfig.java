package com.hbelange.GymProgressTracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class WebAuthorizationConfig {
    
    @Bean
    @Order(3)
    SecurityFilterChain configure(HttpSecurity http) throws Exception {
        
        return http
            .formLogin(form -> form
                .loginPage("/login")
                .permitAll())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/error", "/register").permitAll()
                .anyRequest().authenticated())
            .build();
    }
}
