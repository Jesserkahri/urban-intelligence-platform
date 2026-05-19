package com.urban.intelligence.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Urban Intelligence Platform - Main Application Entry Point
 * 
 * A production-grade civic analytics platform for urban operations,
 * sustainability metrics, and public infrastructure insights.
 */
@SpringBootApplication
@EnableScheduling
public class UrbanIntelligencePlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(UrbanIntelligencePlatformApplication.class, args);
    }
}
