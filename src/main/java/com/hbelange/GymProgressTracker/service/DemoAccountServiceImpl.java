package com.hbelange.GymProgressTracker.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DemoAccountServiceImpl implements DemoAccountService {

    private final String demoUsername;
    private final String demoPassword;

    public DemoAccountServiceImpl(@Value("${app.demo.username:demo}") String demoUsername,
            @Value("${app.demo.password:demo}") String demoPassword) {
        this.demoUsername = demoUsername;
        this.demoPassword = demoPassword;
    }

    @Override
    public String getDemoUsername() {
        return demoUsername;
    }

    @Override
    public String getDemoPassword() {
        return demoPassword;
    }

    @Override
    public boolean isDemoAccount(String username) {
        return demoUsername.equals(username);
    }

}
