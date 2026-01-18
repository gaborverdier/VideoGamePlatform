package com.gaming.platform.service;

import com.gaming.platform.model.Purchase;
import com.gaming.platform.repository.PurchaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service gérant les opérations sur les achats.
 *
 * RESPONSABILITÉS :
 * 1. Fournir les opérations CRUD sur les achats
 * 2. Statistiques d'achats par utilisateur/jeu
 * 3. Historique des transactions
 */
@Service
public class PurchaseService {

    private static final Logger logger = LoggerFactory.getLogger(PurchaseService.class);

    private final PurchaseRepository purchaseRepository;

    public PurchaseService(PurchaseRepository purchaseRepository) {
        this.purchaseRepository = purchaseRepository;
    }

    /**
     * Récupère tous les achats.
     *
     * @return Liste de tous les achats
     */
    public List<Purchase> getAllPurchases() {
        return purchaseRepository.findAll();
    }

    /**
     * Récupère un achat par son ID.
     *
     * @param purchaseId L'identifiant de l'achat
     * @return Optional contenant l'achat si trouvé
     */
    public Optional<Purchase> getPurchaseById(String purchaseId) {
        return purchaseRepository.findById(purchaseId);
    }

    /**
     * Récupère tous les achats d'un utilisateur.
     *
     * @param userId L'identifiant de l'utilisateur
     * @return Liste des achats de cet utilisateur
     */
    public List<Purchase> getPurchasesByUserId(String userId) {
        return purchaseRepository.findByUserId(userId);
    }

    /**
     * Récupère tous les achats d'un jeu.
     *
     * @param gameId L'identifiant du jeu
     * @return Liste des achats de ce jeu
     */
    public List<Purchase> getPurchasesByGameId(String gameId) {
        return purchaseRepository.findByGameId(gameId);
    }

    /**
     * Compte le nombre d'achats pour un jeu.
     *
     * @param gameId L'identifiant du jeu
     * @return Le nombre d'achats
     */
    public long countPurchasesByGame(String gameId) {
        return purchaseRepository.countByGameId(gameId);
    }

    /**
     * Compte le nombre d'achats d'un utilisateur.
     *
     * @param userId L'identifiant de l'utilisateur
     * @return Le nombre d'achats
     */
    public long countPurchasesByUser(String userId) {
        return purchaseRepository.countByUserId(userId);
    }

    /**
     * Récupère les N derniers achats.
     *
     * @param limit Le nombre d'achats à récupérer
     * @return Liste des derniers achats
     */
    public List<Purchase> getRecentPurchases(int limit) {
        return purchaseRepository.findRecentPurchases(limit);
    }

    /**
     * Calcule le montant total dépensé par un utilisateur.
     *
     * @param userId L'identifiant de l'utilisateur
     * @return Le montant total
     */
    public Double getTotalSpentByUser(String userId) {
        return purchaseRepository.getTotalSpentByUser(userId);
    }

    /**
     * Crée un nouvel achat.
     *
     * @param purchase L'achat à créer
     * @return L'achat créé
     */
    @Transactional
    public Purchase createPurchase(Purchase purchase) {
        logger.info("Enregistrement d'un achat: utilisateur {} - jeu {}", 
            purchase.getUserId(), purchase.getGameTitle());
        return purchaseRepository.save(purchase);
    }

    /**
     * Compte le nombre total d'achats.
     *
     * @return Le nombre d'achats
     */
    public long countPurchases() {
        return purchaseRepository.count();
    }

    /**
     * Supprime un achat.
     *
     * @param purchaseId L'identifiant de l'achat à supprimer
     */
    @Transactional
    public void deletePurchase(String purchaseId) {
        logger.info("Suppression de l'achat: {}", purchaseId);
        purchaseRepository.deleteById(purchaseId);
    }
}
