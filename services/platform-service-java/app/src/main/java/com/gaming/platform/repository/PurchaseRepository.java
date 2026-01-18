package com.gaming.platform.repository;

import com.gaming.platform.model.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository JPA pour l'entité Purchase.
 *
 * Fournit les opérations CRUD de base et des méthodes de recherche personnalisées.
 */
@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, String> {

    /**
     * Récupère tous les achats d'un utilisateur.
     *
     * @param userId L'identifiant de l'utilisateur
     * @return Liste des achats de cet utilisateur
     */
    List<Purchase> findByUserId(String userId);

    /**
     * Récupère tous les achats d'un jeu spécifique.
     *
     * @param gameId L'identifiant du jeu
     * @return Liste des achats de ce jeu
     */
    List<Purchase> findByGameId(String gameId);

    /**
     * Compte le nombre d'achats pour un jeu.
     *
     * @param gameId L'identifiant du jeu
     * @return Le nombre d'achats
     */
    long countByGameId(String gameId);

    /**
     * Compte le nombre d'achats d'un utilisateur.
     *
     * @param userId L'identifiant de l'utilisateur
     * @return Le nombre d'achats
     */
    long countByUserId(String userId);

    /**
     * Récupère les N derniers achats (tri par date décroissante).
     *
     * @param limit Le nombre d'achats à récupérer
     * @return Liste des derniers achats
     */
    @Query("SELECT p FROM Purchase p ORDER BY p.purchaseDate DESC LIMIT :limit")
    List<Purchase> findRecentPurchases(int limit);

    /**
     * Calcule le montant total des achats d'un utilisateur.
     *
     * @param userId L'identifiant de l'utilisateur
     * @return Le montant total dépensé
     */
    @Query("SELECT COALESCE(SUM(p.price), 0.0) FROM Purchase p WHERE p.userId = :userId")
    Double getTotalSpentByUser(String userId);
}
