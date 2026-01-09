package com.gaming.publisher.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entité représentant un jeu vidéo dans le catalogue de l'éditeur.
 *
 * Cette classe correspond à une table en base de données et stocke
 * toutes les informations nécessaires sur chaque jeu publié.
 */
@Entity
@Table(name = "games", indexes = {
    @Index(name = "idx_game_title", columnList = "title"),
    @Index(name = "idx_game_publisher", columnList = "publisher")
})
@Data // Lombok : génère getters, setters, toString, equals, hashCode
@NoArgsConstructor // Lombok : génère un constructeur sans argument (requis par JPA)
@AllArgsConstructor // Lombok : génère un constructeur avec tous les arguments
@Builder // Lombok : implémente le pattern Builder pour une création fluide
public class Game {

    /**
     * Identifiant unique du jeu (clé primaire).
     * Généré automatiquement par la base de données.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

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
    @Column(name = "publisher", nullable = false, length = 200)
    private String publisher;

    /**
     * Version actuelle du jeu.
     * Utilise le versioning sémantique : MAJOR.MINOR.PATCH
     * Exemple : "1.0.0", "2.3.5"
     */
    @Column(name = "current_version", nullable = false, length = 20)
    private String currentVersion;

    /**
     * Description du jeu (optionnelle).
     */
    @Column(name = "description", length = 2000)
    private String description;

    /**
     * Date de création de l'enregistrement.
     * Rempli automatiquement lors de la première sauvegarde.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Date de dernière mise à jour de l'enregistrement.
     * Mise à jour automatiquement à chaque modification.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Hook appelé avant la première sauvegarde en base.
     * Initialise les timestamps et la version par défaut.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.currentVersion == null) {
            this.currentVersion = "1.0.0";
        }
    }

    /**
     * Hook appelé avant chaque mise à jour en base.
     * Met à jour le timestamp de modification.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}


