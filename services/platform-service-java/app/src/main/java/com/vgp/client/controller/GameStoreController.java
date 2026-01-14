package com.vgp.client.controller;

import com.vgp.client.dto.response.GameDto;
import com.vgp.client.service.GameStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/client/games")
public class GameStoreController {
    
    private static final Logger logger = LoggerFactory.getLogger(GameStoreController.class);
    
    private final GameStoreService gameStoreService;
    
    public GameStoreController(GameStoreService gameStoreService) {
        this.gameStoreService = gameStoreService;
    }
    
    @GetMapping
    public ResponseEntity<List<GameDto>> getAllGames() {
        logger.info("GET /api/client/games - Fetching all games");
        List<GameDto> games = gameStoreService.findAllGames();
        return ResponseEntity.ok(games);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<GameDto> getGameById(@PathVariable Integer id) {
        logger.info("GET /api/client/games/{} - Fetching game by id", id);
        return gameStoreService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<GameDto>> searchGames(@RequestParam String title) {
        logger.info("GET /api/client/games/search?title={} - Searching games", title);
        List<GameDto> games = gameStoreService.searchByTitle(title);
        return ResponseEntity.ok(games);
    }
    
    @GetMapping("/by-editor/{editorId}")
    public ResponseEntity<List<GameDto>> getGamesByEditor(@PathVariable Integer editorId) {
        logger.info("GET /api/client/games/by-editor/{} - Fetching games by editor", editorId);
        List<GameDto> games = gameStoreService.findByEditorId(editorId);
        return ResponseEntity.ok(games);
    }
}
