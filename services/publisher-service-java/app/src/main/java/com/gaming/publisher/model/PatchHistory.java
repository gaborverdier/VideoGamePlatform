package com.gaming.publisher.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entité représentant l'historique des patches publiés pour un jeu.
 *
 * Permet de tracer toutes les mises à jour et corrections déployées
 * sur chaque jeu du catalogue.
 */
@Entity
@Table(name = "patch_history", indexes = {
    @Index(name = "idx_patch_game_id", columnList = "game_id"),
    @Index(name = "idx_patch_date", columnList = "release_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatchHistory {

    /**
     * Identifiant unique du patch.
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
     * Titre du jeu (dénormalisé).
     */
    @Column(name = "game_title", length = 500)
    private String gameTitle;

    /**
     * Nouvelle version déployée.
     * Exemple : "1.2.3"
     */
    @Column(name = "version", nullable = false, length = 20)
    private String version;

    /**
     * Version précédente (avant le patch).
     */
    @Column(name = "previous_version", length = 20)
    private String previousVersion;

    /**
     * Description des changements (changelog).
     * Exemple : "- Fixed memory leak in level 3\n- Improved graphics performance"
     */
    @Column(name = "changelog", columnDefinition = "TEXT")
    private String changelog;

    /**
     * Taille du patch en octets.
     */
    @Column(name = "patch_size")
    private Long patchSize;

    /**
     * Date de publication du patch.
     */
    @Column(name = "release_date", nullable = false)
    private LocalDateTime releaseDate;

    @PrePersist
    protected void onCreate() {
        if (this.releaseDate == null) {
            this.releaseDate = LocalDateTime.now();
        }
    }
}

