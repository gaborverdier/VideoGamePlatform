package com.controller;

import com.model.Game;
import com.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/games")
public class GameController {
    @Autowired
    private GameService gameService;

    @GetMapping
    public ResponseEntity<List<Game>> getAllGames() {
        List<Game> games = gameService.getAllGames();
        return ResponseEntity.ok(games);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Game> getGameById(@PathVariable Long id) {
        return gameService.getGameById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/publisher/{publisherId}")
    public ResponseEntity<List<Game>> getGamesByPublisher(@PathVariable Long publisherId) {
        List<Game> games = gameService.getGamesByPublisher(publisherId);
        return ResponseEntity.ok(games);
    }

    @PostMapping("/publisher/{publisherId}")
    public ResponseEntity<Game> createGame(@PathVariable("publisherId") Long publisherId, @RequestBody Game game) {
        Game created = gameService.createGame(publisherId, game);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Game> updateGame(@PathVariable Long id, @RequestBody Game game) {
        Game updated = gameService.updateGame(id, game);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGame(@PathVariable Long id) {
        gameService.deleteGame(id);
        return ResponseEntity.noContent().build();
    }
}
