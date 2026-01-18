package com.gaming.platform.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entité représentant un jeu vidéo dans le catalogue de la plateforme.
 *
 * Cette classe stocke les informations sur les jeux disponibles,
 * synchronisées depuis les événements du Publisher Service.
 */
@Entity
@Table(name = "platform_games", indexes = {
    @Index(name = "idx_game_title", columnList = "title"),
    @Index(name = "idx_game_publisher", columnList = "publisher")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Game {

    /**
     * Identifiant unique du jeu (clé primaire).
     */
    @Id
    @Column(name = "game_id", nullable = false, updatable = false, length = 100)
    private String gameId;

    /**
     * Titre du jeu.
     * Exemple : "The Legend of Zelda: Breath of the Wild"
     */
    @Column(name = "title", nullable = false, length = 500)
    private String title;

    /**
     * Genre du jeu.
     * Exemple : "Action", "RPG", "Sports", "Racing"
     */
    @Column(name = "genre", length = 100)
    private String genre;

    /**
     * Plateforme de publication.
     * Exemple : "PS4", "Xbox", "PC", "Switch"
     */
    @Column(name = "platform", length = 100)
    private String platform;

    /**
     * Nom de l'éditeur propriétaire du jeu.
     * Exemple : "Nintendo", "Activision", "Electronic Arts"
     */
    @Column(name = "publisher", length = 200)
    private String publisher;

    /**
     * Version actuelle du jeu.
     * Utilise le versioning sémantique : MAJOR.MINOR.PATCH
     * Exemple : "1.0.0", "2.3.5"
     */
    @Column(name = "current_version", length = 20)
    private String currentVersion;

    /**
     * Description du jeu (optionnelle).
     */
    @Column(name = "description", length = 2000)
    private String description;

    /**
     * Date de création de l'enregistrement.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Date de dernière mise à jour de l'enregistrement.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
