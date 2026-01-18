package com.gaming.platform.controller;

import com.gaming.platform.model.Game;
import com.gaming.platform.service.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller pour la gestion des jeux.
 *
 * ENDPOINTS DISPONIBLES :
 * GET    /api/games                     Liste tous les jeux
 * GET    /api/games/{id}                Détails d'un jeu
 * GET    /api/games/search?title=X      Recherche par titre
 * GET    /api/games/publisher/{name}    Jeux d'un éditeur
 * GET    /api/games/genre/{genre}       Jeux d'un genre
 * GET    /api/games/platform/{platform} Jeux d'une plateforme
 * GET    /api/games/stats               Statistiques des jeux
 */
@RestController
@RequestMapping("/api/games")
public class GameController {

    private static final Logger logger = LoggerFactory.getLogger(GameController.class);

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    /**
     * Liste tous les jeux du catalogue.
     *
     * GET /api/games
     */
    @GetMapping
    public ResponseEntity<List<Game>> getAllGames() {
        List<Game> games = gameService.getAllGames();
        logger.info("Récupération de {} jeux", games.size());
        return ResponseEntity.ok(games);
    }

    /**
     * Récupère les détails d'un jeu.
     *
     * GET /api/games/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Game> getGameById(@PathVariable String id) {
        return gameService.getGameById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Recherche des jeux par titre.
     *
     * GET /api/games/search?title=zelda
     */
    @GetMapping("/search")
    public ResponseEntity<List<Game>> searchGames(@RequestParam String title) {
        List<Game> games = gameService.searchGamesByTitle(title);
        return ResponseEntity.ok(games);
    }

    /**
     * Récupère les jeux d'un éditeur.
     *
     * GET /api/games/publisher/Nintendo
     */
    @GetMapping("/publisher/{publisher}")
    public ResponseEntity<List<Game>> getGamesByPublisher(@PathVariable String publisher) {
        List<Game> games = gameService.getGamesByPublisher(publisher);
        return ResponseEntity.ok(games);
    }

    /**
     * Récupère les jeux d'un genre.
     *
     * GET /api/games/genre/Action
     */
    @GetMapping("/genre/{genre}")
    public ResponseEntity<List<Game>> getGamesByGenre(@PathVariable String genre) {
        List<Game> games = gameService.getGamesByGenre(genre);
        return ResponseEntity.ok(games);
    }

    /**
     * Récupère les jeux d'une plateforme.
     *
     * GET /api/games/platform/PS4
     */
    @GetMapping("/platform/{platform}")
    public ResponseEntity<List<Game>> getGamesByPlatform(@PathVariable String platform) {
        List<Game> games = gameService.getGamesByPlatform(platform);
        return ResponseEntity.ok(games);
    }

    /**
     * Statistiques des jeux.
     *
     * GET /api/games/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getGameStats() {
        long totalGames = gameService.countGames();

        Map<String, Object> stats = Map.of(
            "totalGames", totalGames,
            "message", "Statistiques du catalogue de jeux"
        );

        return ResponseEntity.ok(stats);
    }
}
