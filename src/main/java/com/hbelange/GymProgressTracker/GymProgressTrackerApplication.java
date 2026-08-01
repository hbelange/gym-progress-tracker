package com.hbelange.GymProgressTracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class GymProgressTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(GymProgressTrackerApplication.class, args);
	}

}
