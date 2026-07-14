package com.hbelange.GymProgressTracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class WebAuthorizationConfig {
    
    private final AuthenticationProvider authenticationProvider;

    public WebAuthorizationConfig(AuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
    }

    @Bean
    @Order(2)
    SecurityFilterChain configure(HttpSecurity http) throws Exception {
        
        return http
            .formLogin(c -> c.defaultSuccessUrl("/", true))
            .httpBasic(Customizer.withDefaults())
            .authenticationProvider(authenticationProvider)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/error").permitAll()
                .anyRequest().authenticated())
            .build();
    }
}
