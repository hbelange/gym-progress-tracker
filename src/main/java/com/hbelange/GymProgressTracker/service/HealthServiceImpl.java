package com.hbelange.GymProgressTracker.service;

import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;

@Service
public class HealthServiceImpl implements HealthService {

    private final DataSource dataSource;

    public HealthServiceImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public String getHealth() {
        try (Connection conn = dataSource.getConnection()) {
            return conn.isValid(2) ? "UP" : "DOWN";
        } catch (Exception e) {
            return "DOWN";
        }
    }
}
