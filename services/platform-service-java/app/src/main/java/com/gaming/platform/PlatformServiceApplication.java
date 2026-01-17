package com.gaming.platform;

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
public class PlatformServiceApplication {
    public String getGreeting() {
        return "\n#########################\nPlatform Service Started (client side)\n#########################\n";
    }

    public static void main(String[] args) {
        SpringApplication.run(PlatformServiceApplication.class, args);
        System.out.println(new PlatformServiceApplication().getGreeting());
    }
}
