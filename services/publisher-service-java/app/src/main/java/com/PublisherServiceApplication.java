package com;

import org.springframework. boot.SpringApplication;
import org.springframework.boot.autoconfigure. SpringBootApplication;
import org. springframework.scheduling.annotation.EnableScheduling;


/**
 * Platform Service - Main entry point
 *
 * This service handles player-side interactions: 
 * - User registration and management
 * - Game catalog browsing
 * - Game purchases
 * - User library management
 * - Event consumption from publisher service
 *
 * @author Platform Service Team
 * @version 1.0.0
 */

@SpringBootApplication
@EnableScheduling
public class PublisherServiceApplication {
    public String getGreeting() {
        return "\n#########################\nPublisher Service Started (Publisher side)\n#########################\n";
    }

    public static void main(String[] args) {
        // Lancer Spring Boot dans un thread séparé
        new Thread(() -> {
            SpringApplication.run(PublisherServiceApplication.class, args);
        }).start();
        
        System.out.println(new PublisherServiceApplication().getGreeting());
        
        // Lancer JavaFX dans le thread principal
        PublisherSimulatorApplication.main(args);
    }
}
