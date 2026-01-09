package com.gaming.publisher.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entité représentant un rapport de crash reçu pour un jeu.
 *
 * Stocke les informations sur les bugs et erreurs signalés par les utilisateurs
 * ou détectés automatiquement sur la plateforme.
 */
@Entity
@Table(name = "crash_reports", indexes = {
    @Index(name = "idx_crash_game_id", columnList = "game_id"),
    @Index(name = "idx_crash_timestamp", columnList = "crash_timestamp")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrashReport {

    /**
     * Identifiant unique du rapport de crash.
     */
    @Id
    @Column(name = "crash_id", nullable = false, updatable = false, length = 100)
    private String crashId;

    /**
     * Identifiant du jeu concerné par le crash.
     */
    @Column(name = "game_id", nullable = false, length = 100)
    private String gameId;

    /**
     * Titre du jeu (dénormalisé pour faciliter les requêtes).
     */
    @Column(name = "game_title", length = 500)
    private String gameTitle;

    /**
     * Code d'erreur technique.
     * Exemple : "ERR_MEMORY_LEAK", "ERR_NULL_POINTER"
     */
    @Column(name = "error_code", nullable = false, length = 100)
    private String errorCode;

    /**
     * Message d'erreur détaillé.
     */
    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    /**
     * Stack trace complet (optionnel).
     * Utile pour le debugging approfondi.
     */
    @Column(name = "stack_trace", columnDefinition = "TEXT")
    private String stackTrace;

    /**
     * Plateforme où le crash s'est produit.
     * Exemple : "PS4", "Xbox", "PC"
     */
    @Column(name = "platform", length = 100)
    private String platform;

    /**
     * Version du jeu lors du crash.
     * Permet de suivre si un patch a résolu le problème.
     */
    @Column(name = "game_version", length = 20)
    private String gameVersion;

    /**
     * Date et heure du crash.
     */
    @Column(name = "crash_timestamp", nullable = false)
    private LocalDateTime crashTimestamp;

    /**
     * ID de l'utilisateur affecté (optionnel).
     */
    @Column(name = "user_id", length = 100)
    private String userId;

    /**
     * Date de réception du rapport.
     */
    @Column(name = "received_at", nullable = false, updatable = false)
    private LocalDateTime receivedAt;

    @PrePersist
    protected void onCreate() {
        this.receivedAt = LocalDateTime.now();
    }
}

