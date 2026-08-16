package com.hbelange.GymProgressTracker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import com.hbelange.GymProgressTracker.TestcontainersConfiguration;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class DemoAccountServiceTest {

    @Autowired
    private DemoAccountService demoAccountService;

    @Test
    void isDemoAccountTrueForConfiguredDemoUsername() {
        assertTrue(demoAccountService.isDemoAccount("demo"));
    }

    @Test
    void isDemoAccountFalseForOtherUsername() {
        assertFalse(demoAccountService.isDemoAccount("someone-else"));
    }

    @Test
    void getDemoPasswordReturnsConfiguredValue() {
        assertEquals("demo", demoAccountService.getDemoPassword());
    }

}
