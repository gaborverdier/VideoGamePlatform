package com.gaming.platform.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entité représentant un achat de jeu par un utilisateur.
 *
 * Cette classe stocke l'historique des achats/transactions
 * effectuées sur la plateforme.
 */
@Entity
@Table(name = "platform_purchases", indexes = {
    @Index(name = "idx_purchase_user", columnList = "user_id"),
    @Index(name = "idx_purchase_game", columnList = "game_id"),
    @Index(name = "idx_purchase_date", columnList = "purchase_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Purchase {

    /**
     * Identifiant unique de l'achat (clé primaire).
     * Généré automatiquement par la base de données.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "purchase_id", nullable = false, updatable = false)
    private String purchaseId;

    /**
     * Identifiant de l'utilisateur ayant effectué l'achat.
     */
    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    /**
     * Identifiant du jeu acheté.
     */
    @Column(name = "game_id", nullable = false, length = 100)
    private String gameId;

    /**
     * Titre du jeu (dénormalisé pour faciliter les requêtes).
     */
    @Column(name = "game_title", length = 500)
    private String gameTitle;

    /**
     * Nom d'utilisateur (dénormalisé).
     */
    @Column(name = "username", length = 100)
    private String username;

    /**
     * Prix payé pour le jeu.
     */
    @Column(name = "price")
    private Double price;

    /**
     * Date de l'achat.
     */
    @Column(name = "purchase_date", nullable = false)
    private LocalDateTime purchaseDate;

    /**
     * Timestamp de l'événement d'achat (en millisecondes depuis epoch).
     */
    @Column(name = "event_timestamp")
    private Long eventTimestamp;

    /**
     * Date de création de l'enregistrement.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.purchaseDate == null) {
            this.purchaseDate = LocalDateTime.now();
        }
    }
}
