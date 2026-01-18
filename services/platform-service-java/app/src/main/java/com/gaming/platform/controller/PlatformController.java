package com.gaming.platform.controller;

import com.gaming.platform.service.GameService;
import com.gaming.platform.service.PurchaseService;
import com.gaming.platform.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST Controller pour les statistiques globales de la plateforme.
 *
 * ENDPOINTS DISPONIBLES :
 * GET    /api/stats              Statistiques globales
 * GET    /api/health             Health check
 */
@RestController
@RequestMapping("/api")
public class PlatformController {

    private final UserService userService;
    private final GameService gameService;
    private final PurchaseService purchaseService;

    public PlatformController(
            UserService userService,
            GameService gameService,
            PurchaseService purchaseService) {
        this.userService = userService;
        this.gameService = gameService;
        this.purchaseService = purchaseService;
    }

    /**
     * Statistiques globales de la plateforme.
     *
     * GET /api/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getGlobalStats() {
        long totalUsers = userService.countUsers();
        long totalGames = gameService.countGames();
        long totalPurchases = purchaseService.countPurchases();

        Map<String, Object> stats = Map.of(
            "platform", "Video Game Platform",
            "totalUsers", totalUsers,
            "totalGames", totalGames,
            "totalPurchases", totalPurchases,
            "status", "operational"
        );

        return ResponseEntity.ok(stats);
    }

    /**
     * Health check de l'API.
     *
     * GET /api/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> health = Map.of(
            "status", "UP",
            "service", "Platform Service",
            "message", "Service opérationnel"
        );

        return ResponseEntity.ok(health);
    }
}
