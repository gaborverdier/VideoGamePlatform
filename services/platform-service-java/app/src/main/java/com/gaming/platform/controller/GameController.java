package com.gaming.platform.controller;

import com.gaming.platform.dto.GameDTO;
import com.gaming.platform.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @GetMapping
    public ResponseEntity<List<GameDTO>> getAllGames() {
        return ResponseEntity.ok(gameService.getAllAvailableGames());
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameDTO> getGame(@PathVariable String gameId) {
        return gameService.getGameById(gameId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<GameDTO>> searchGames(@RequestParam String title) {
        return ResponseEntity.ok(gameService.searchGames(title));
    }

    @GetMapping("/genre/{genre}")
    public ResponseEntity<List<GameDTO>> getGamesByGenre(@PathVariable String genre) {
        return ResponseEntity.ok(gameService.getGamesByGenre(genre));
    }

    @GetMapping("/platform/{platform}")
    public ResponseEntity<List<GameDTO>> getGamesByPlatform(@PathVariable String platform) {
        return ResponseEntity.ok(gameService.getGamesByPlatform(platform));
    }
}