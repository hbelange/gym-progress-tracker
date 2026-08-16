package com.hbelange.GymProgressTracker.service;

public interface DemoAccountService {
    public String getDemoUsername();
    public String getDemoPassword();
    public boolean isDemoAccount(String username);
}
