package com.hbelange.GymProgressTracker.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.hbelange.GymProgressTracker.service.DemoDataSeeder;

@Component
public class DemoResetScheduler {

    private final DemoDataSeeder demoDataSeeder;

    public DemoResetScheduler(DemoDataSeeder demoDataSeeder) {
        this.demoDataSeeder = demoDataSeeder;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seedOnStartup() {
        demoDataSeeder.reseed();
    }

    @Scheduled(cron = "0 0 * * * *")
    public void resetHourly() {
        demoDataSeeder.reseed();
    }

}
