package com.gaming.platform.controller;

import com.gaming.platform.model.Purchase;
import com.gaming.platform.service.PurchaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller pour la gestion des achats.
 *
 * ENDPOINTS DISPONIBLES :
 * GET    /api/purchases              Liste tous les achats
 * GET    /api/purchases/{id}         Détails d'un achat
 * GET    /api/purchases/user/{userId}  Achats d'un utilisateur
 * GET    /api/purchases/game/{gameId}  Achats d'un jeu
 * GET    /api/purchases/recent?limit=X Derniers achats
 * GET    /api/purchases/stats        Statistiques des achats
 */
@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {

    private static final Logger logger = LoggerFactory.getLogger(PurchaseController.class);

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    /**
     * Liste tous les achats.
     *
     * GET /api/purchases
     */
    @GetMapping
    public ResponseEntity<List<Purchase>> getAllPurchases() {
        List<Purchase> purchases = purchaseService.getAllPurchases();
        logger.info("Récupération de {} achats", purchases.size());
        return ResponseEntity.ok(purchases);
    }

    /**
     * Récupère les détails d'un achat.
     *
     * GET /api/purchases/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Purchase> getPurchaseById(@PathVariable String id) {
        return purchaseService.getPurchaseById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Récupère tous les achats d'un utilisateur.
     *
     * GET /api/purchases/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Purchase>> getPurchasesByUser(@PathVariable String userId) {
        List<Purchase> purchases = purchaseService.getPurchasesByUserId(userId);
        return ResponseEntity.ok(purchases);
    }

    /**
     * Récupère tous les achats d'un jeu.
     *
     * GET /api/purchases/game/{gameId}
     */
    @GetMapping("/game/{gameId}")
    public ResponseEntity<List<Purchase>> getPurchasesByGame(@PathVariable String gameId) {
        List<Purchase> purchases = purchaseService.getPurchasesByGameId(gameId);
        return ResponseEntity.ok(purchases);
    }

    /**
     * Récupère les N derniers achats.
     *
     * GET /api/purchases/recent?limit=10
     */
    @GetMapping("/recent")
    public ResponseEntity<List<Purchase>> getRecentPurchases(
            @RequestParam(defaultValue = "10") int limit) {
        List<Purchase> purchases = purchaseService.getRecentPurchases(limit);
        return ResponseEntity.ok(purchases);
    }

    /**
     * Statistiques des achats.
     *
     * GET /api/purchases/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getPurchaseStats() {
        long totalPurchases = purchaseService.countPurchases();

        Map<String, Object> stats = Map.of(
            "totalPurchases", totalPurchases,
            "message", "Statistiques des achats sur la plateforme"
        );

        return ResponseEntity.ok(stats);
    }

    /**
     * Statistiques d'achats d'un utilisateur.
     *
     * GET /api/purchases/user/{userId}/stats
     */
    @GetMapping("/user/{userId}/stats")
    public ResponseEntity<Map<String, Object>> getUserPurchaseStats(@PathVariable String userId) {
        long purchaseCount = purchaseService.countPurchasesByUser(userId);
        Double totalSpent = purchaseService.getTotalSpentByUser(userId);

        Map<String, Object> stats = Map.of(
            "userId", userId,
            "purchaseCount", purchaseCount,
            "totalSpent", totalSpent != null ? totalSpent : 0.0
        );

        return ResponseEntity.ok(stats);
    }
}
