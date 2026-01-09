package com.gaming.publisher.controller;

import com.gaming.publisher.model.CrashReport;
import com.gaming.publisher.model.Game;
import com.gaming.publisher.model.PatchHistory;
import com.gaming.publisher.model.ReviewStats;
import com.gaming.publisher.repository.CrashReportRepository;
import com.gaming.publisher.repository.GameRepository;
import com.gaming.publisher.repository.PatchHistoryRepository;
import com.gaming.publisher.repository.ReviewStatsRepository;
import com.gaming.publisher.service.AutoPatchSimulatorService;
import com.gaming.publisher.service.MetadataService;
import com.gaming.publisher.service.PatchService;
import com.gaming.publisher.service.VGSalesLoaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller exposant l'API du Publisher Service.
 *
 * ENDPOINTS DISPONIBLES :
 *
 * --- GAMES ---
 * GET    /api/games                  Liste tous les jeux
 * GET    /api/games/{id}             Détails d'un jeu
 * GET    /api/games/search?title=X   Recherche par titre
 *
 * --- PATCHES ---
 * POST   /api/games/{id}/patch       Publie un patch
 * GET    /api/games/{id}/patches     Historique des patches
 *
 * --- METADATA ---
 * PUT    /api/games/{id}/metadata    Met à jour les métadonnées
 *
 * --- CRASHES ---
 * GET    /api/crashes                Liste tous les crashes
 * GET    /api/crashes/game/{id}      Crashes d'un jeu spécifique
 * GET    /api/crashes/stats          Statistiques de crashes
 *
 * --- REVIEWS ---
 * GET    /api/reviews                Liste toutes les stats de notes
 * GET    /api/reviews/game/{id}      Stats d'un jeu spécifique
 *
 * --- ADMIN ---
 * POST   /api/admin/reload-vgsales   Recharge les données VGSales
 * POST   /api/admin/simulate-patch   Simule un patch aléatoire
 * GET    /api/admin/stats            Statistiques globales
 */
@RestController
@RequestMapping("/api")
public class PublisherController {

    private static final Logger logger = LoggerFactory.getLogger(PublisherController.class);

    private final GameRepository gameRepository;
    private final PatchHistoryRepository patchHistoryRepository;
    private final CrashReportRepository crashReportRepository;
    private final ReviewStatsRepository reviewStatsRepository;
    private final PatchService patchService;
    private final MetadataService metadataService;
    private final VGSalesLoaderService vgSalesLoader;
    private final AutoPatchSimulatorService patchSimulator;

    public PublisherController(
            GameRepository gameRepository,
            PatchHistoryRepository patchHistoryRepository,
            CrashReportRepository crashReportRepository,
            ReviewStatsRepository reviewStatsRepository,
            PatchService patchService,
            MetadataService metadataService,
            VGSalesLoaderService vgSalesLoader,
            AutoPatchSimulatorService patchSimulator) {

        this.gameRepository = gameRepository;
        this.patchHistoryRepository = patchHistoryRepository;
        this.crashReportRepository = crashReportRepository;
        this.reviewStatsRepository = reviewStatsRepository;
        this.patchService = patchService;
        this.metadataService = metadataService;
        this.vgSalesLoader = vgSalesLoader;
        this.patchSimulator = patchSimulator;
    }

    // ========== GAMES ==========

    /**
     * Liste tous les jeux du catalogue.
     *
     * GET /api/games
     */
    @GetMapping("/games")
    public ResponseEntity<List<Game>> getAllGames() {
        List<Game> games = gameRepository.findAll();
        logger.info("Récupération de {} jeux", games.size());
        return ResponseEntity.ok(games);
    }

    /**
     * Récupère les détails d'un jeu.
     *
     * GET /api/games/{id}
     */
    @GetMapping("/games/{id}")
    public ResponseEntity<Game> getGameById(@PathVariable String id) {
        return gameRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Recherche des jeux par titre.
     *
     * GET /api/games/search?title=zelda
     */
    @GetMapping("/games/search")
    public ResponseEntity<List<Game>> searchGames(@RequestParam String title) {
        List<Game> games = gameRepository.findByTitleContainingIgnoreCase(title);
        return ResponseEntity.ok(games);
    }

    // ========== PATCHES ==========

    /**
     * Publie un patch pour un jeu.
     *
     * POST /api/games/{id}/patch
     * Body: { "changelog": "Description des changements" }
     */
    @PostMapping("/games/{id}/patch")
    public ResponseEntity<?> publishPatch(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {

        try {
            String changelog = body.getOrDefault("changelog", patchService.generateRandomChangelog());
            PatchHistory patch = patchService.deployPatch(id, changelog);

            logger.info("Patch publié via API pour le jeu: {}", id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Patch publié avec succès",
                "patch", patch
            ));
        } catch (Exception e) {
            logger.error("Erreur publication patch: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Récupère l'historique des patches d'un jeu.
     *
     * GET /api/games/{id}/patches
     */
    @GetMapping("/games/{id}/patches")
    public ResponseEntity<List<PatchHistory>> getGamePatches(@PathVariable String id) {
        List<PatchHistory> patches = patchHistoryRepository.findByGameIdOrderByReleaseDateDesc(id);
        return ResponseEntity.ok(patches);
    }

    // ========== METADATA ==========

    /**
     * Met à jour les métadonnées d'un jeu.
     *
     * PUT /api/games/{id}/metadata
     * Body: { "genre": "Action", "platform": "PS5", "description": "..." }
     */
    @PutMapping("/games/{id}/metadata")
    public ResponseEntity<?> updateMetadata(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {

        try {
            String genre = body.get("genre");
            String platform = body.get("platform");
            String description = body.get("description");

            Game game = metadataService.updateMetadata(id, genre, platform, description);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Métadonnées mises à jour",
                "game", game
            ));
        } catch (Exception e) {
            logger.error("Erreur mise à jour métadonnées: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    // ========== CRASHES ==========

    /**
     * Liste tous les rapports de crash.
     *
     * GET /api/crashes
     */
    @GetMapping("/crashes")
    public ResponseEntity<List<CrashReport>> getAllCrashes() {
        List<CrashReport> crashes = crashReportRepository.findAll();
        return ResponseEntity.ok(crashes);
    }

    /**
     * Récupère les crashes d'un jeu spécifique.
     *
     * GET /api/crashes/game/{id}
     */
    @GetMapping("/crashes/game/{id}")
    public ResponseEntity<List<CrashReport>> getGameCrashes(@PathVariable String id) {
        List<CrashReport> crashes = crashReportRepository.findByGameIdOrderByCrashTimestampDesc(id);
        return ResponseEntity.ok(crashes);
    }

    /**
     * Statistiques de crashes.
     *
     * GET /api/crashes/stats
     */
    @GetMapping("/crashes/stats")
    public ResponseEntity<?> getCrashStats() {
        long totalCrashes = crashReportRepository.count();
        List<Object[]> gamesWithMostCrashes = crashReportRepository
            .findGamesWithCrashesAboveThreshold(5);

        return ResponseEntity.ok(Map.of(
            "totalCrashes", totalCrashes,
            "gamesWithMostCrashes", gamesWithMostCrashes
        ));
    }

    // ========== REVIEWS ==========

    /**
     * Liste toutes les statistiques de notes.
     *
     * GET /api/reviews
     */
    @GetMapping("/reviews")
    public ResponseEntity<List<ReviewStats>> getAllReviews() {
        List<ReviewStats> reviews = reviewStatsRepository.findAll();
        return ResponseEntity.ok(reviews);
    }

    /**
     * Récupère les stats de notes d'un jeu.
     *
     * GET /api/reviews/game/{id}
     */
    @GetMapping("/reviews/game/{id}")
    public ResponseEntity<List<ReviewStats>> getGameReviews(@PathVariable String id) {
        List<ReviewStats> reviews = reviewStatsRepository
            .findByGameIdOrderByAggregationTimestampDesc(id);
        return ResponseEntity.ok(reviews);
    }

    // ========== ADMIN ==========

    /**
     * Recharge les données VGSales.
     *
     * POST /api/admin/reload-vgsales
     */
    @PostMapping("/admin/reload-vgsales")
    public ResponseEntity<?> reloadVGSales() {
        try {
            long count = vgSalesLoader.reloadData();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Données VGSales rechargées",
                "gamesLoaded", count
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Déclenche une simulation de patch aléatoire.
     *
     * POST /api/admin/simulate-patch
     */
    @PostMapping("/admin/simulate-patch")
    public ResponseEntity<?> simulatePatch() {
        try {
            patchSimulator.triggerManualSimulation();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Simulation de patch déclenchée"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Statistiques globales du service.
     *
     * GET /api/admin/stats
     */
    @GetMapping("/admin/stats")
    public ResponseEntity<?> getGlobalStats() {
        long totalGames = gameRepository.count();
        long totalPatches = patchHistoryRepository.count();
        long totalCrashes = crashReportRepository.count();
        long totalReviews = reviewStatsRepository.count();

        return ResponseEntity.ok(Map.of(
            "totalGames", totalGames,
            "totalPatches", totalPatches,
            "totalCrashes", totalCrashes,
            "totalReviews", totalReviews
        ));
    }
}

