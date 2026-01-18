package com.gaming.platform.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entité représentant un utilisateur de la plateforme de gaming.
 *
 * Cette classe correspond à une table en base de données et stocke
 * toutes les informations sur les utilisateurs inscrits.
 */
@Entity
@Table(name = "platform_users", indexes = {
    @Index(name = "idx_user_username", columnList = "username"),
    @Index(name = "idx_user_email", columnList = "email")
})
@Data // Lombok : génère getters, setters, toString, equals, hashCode
@NoArgsConstructor // Lombok : génère un constructeur sans argument (requis par JPA)
@AllArgsConstructor // Lombok : génère un constructeur avec tous les arguments
@Builder // Lombok : implémente le pattern Builder pour une création fluide
public class User {

    /**
     * Identifiant unique de l'utilisateur (clé primaire).
     */
    @Id
    @Column(name = "user_id", nullable = false, updatable = false, length = 100)
    private String userId;

    /**
     * Nom d'utilisateur (unique).
     * Exemple : "player123", "gamer_pro"
     */
    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    /**
     * Adresse email de l'utilisateur (unique).
     */
    @Column(name = "email", nullable = false, unique = true, length = 200)
    private String email;

    /**
     * Timestamp d'inscription (en millisecondes depuis epoch).
     */
    @Column(name = "registration_timestamp", nullable = false)
    private Long registrationTimestamp;

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
     * Initialise les timestamps.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
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
