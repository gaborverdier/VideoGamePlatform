package com.gaming.publisher.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entité représentant les statistiques agrégées des notes d'un jeu.
 *
 * Stocke les données reçues du service d'analytics pour suivre
 * la perception qualité des jeux par les joueurs.
 */
@Entity
@Table(name = "review_stats", indexes = {
    @Index(name = "idx_review_game_id", columnList = "game_id"),
    @Index(name = "idx_review_timestamp", columnList = "aggregation_timestamp")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewStats {

    /**
     * Identifiant unique des statistiques.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    /**
     * Identifiant du jeu concerné.
     */
    @Column(name = "game_id", nullable = false, length = 100)
    private String gameId;

    /**
     * Titre du jeu.
     */
    @Column(name = "game_title", length = 500)
    private String gameTitle;

    /**
     * Note moyenne calculée (0.0 - 5.0).
     */
    @Column(name = "average_rating", nullable = false)
    private Double averageRating;

    /**
     * Nombre total de notes reçues.
     */
    @Column(name = "total_ratings", nullable = false)
    private Long totalRatings;

    /**
     * Date et heure de l'agrégation.
     */
    @Column(name = "aggregation_timestamp", nullable = false)
    private LocalDateTime aggregationTimestamp;

    /**
     * Début de la fenêtre temporelle analysée.
     */
    @Column(name = "time_window_start")
    private LocalDateTime timeWindowStart;

    /**
     * Fin de la fenêtre temporelle analysée.
     */
    @Column(name = "time_window_end")
    private LocalDateTime timeWindowEnd;

    /**
     * Date de réception des stats.
     */
    @Column(name = "received_at", nullable = false, updatable = false)
    private LocalDateTime receivedAt;

    @PrePersist
    protected void onCreate() {
        this.receivedAt = LocalDateTime.now();
    }
}

