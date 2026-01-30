package com.gaming.platform.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class DatabaseConfig {

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("===========================================");
        log.info("Platform Service Database Initialized");
        log.info("===========================================");
        log.info("H2 Console:  http://localhost:8082/h2-console");
        log.info("JDBC URL: jdbc:h2:mem:platformdb");
        log.info("Username: sa");
        log.info("Password: (empty)");
        log.info("===========================================");
    }
}