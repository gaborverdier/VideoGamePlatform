package com.gaming.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Point d'entrée principal du Platform Service.
 *
 * Ce service agit comme le système central de la plateforme de gaming qui :
 * - Gère les utilisateurs inscrits sur la plateforme
 * - Maintient le catalogue de jeux disponibles
 * - Suit les achats et transactions des utilisateurs
 * - Consomme les événements des autres services (Publisher, etc.)
 *
 * @author Platform Service Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableScheduling // Active la planification de tâches périodiques
public class PlatformServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlatformServiceApplication.class, args);
    }
}
