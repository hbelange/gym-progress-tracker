package com.hbelange.GymProgressTracker.config;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@Configuration
@EnableMethodSecurity
public class WebAuthorizationConfig {

    @Bean
    SecurityFilterChain configure(HttpSecurity http) throws Exception {

        return http
            .requestCache(cache -> cache.requestCache(navigationOnlyRequestCache()))
            .formLogin(form -> form
                .loginPage("/login")
                .failureHandler(loginFailureHandler())
                .permitAll())
            .exceptionHandling(exceptions -> exceptions
                .defaultAuthenticationEntryPointFor(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                    PathPatternRequestMatcher.withDefaults().matcher("/api/**"))
                .defaultAuthenticationEntryPointFor(
                    new LoginUrlAuthenticationEntryPoint("/login"),
                    PathPatternRequestMatcher.withDefaults().matcher("/**")))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/error", "/register", "/register/pending", "/login", "/css/**", "/verify", "/resend-verification", "/forgot-password", "/reset-password").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/register").permitAll()
                .anyRequest().authenticated())
            .build();
    }

    private RequestCache navigationOnlyRequestCache() {
        /*
         * Browsers auto-request subresources like /apple-touch-icon.png or /favicon.ico on
         * every page load. Left unfiltered, an unauthenticated probe for one of those can be
         * cached as the "saved request" and clobber the page the user actually meant to visit,
         * so the post-login redirect sends them to an icon URL instead. Only top-level page
         * navigations (Sec-Fetch-Mode: navigate) are eligible to be saved and replayed.
        */
        HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
        requestCache.setRequestMatcher(request -> "navigate".equals(request.getHeader("Sec-Fetch-Mode")));
        return requestCache;
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
