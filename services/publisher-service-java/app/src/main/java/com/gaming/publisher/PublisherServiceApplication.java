package com.gaming.publisher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Point d'entrée principal du Publisher Service.
 *
 * Ce service simule le comportement d'un éditeur de jeux vidéo qui :
 * - Gère son catalogue de jeux (initialisé depuis VGSales)
 * - Publie des mises à jour (patches) et modifications de métadonnées
 * - Analyse les rapports de crash et les statistiques de qualité
 *
 * @author Publisher Service Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableScheduling // Active la planification de tâches périodiques
public class PublisherServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PublisherServiceApplication.class, args);
    }
}

