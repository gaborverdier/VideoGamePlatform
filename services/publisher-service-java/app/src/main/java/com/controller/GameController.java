package com.controller;

import com.gaming.api.models.GameModel;
import com.mapper.GameMapper;
import com.model.Game;
import com.model.Publisher;
import com.repository.PublisherRepository;
import com.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

import java.util.List;

@RestController
@RequestMapping("/api/games")
public class GameController {
    @Autowired
    private GameService gameService;
    @Autowired
    private GameMapper gameMapper;
    @Autowired
    private PublisherRepository publisherRepository;

    @GetMapping
    public ResponseEntity<List<GameModel>> getAllGames() {
        return ResponseEntity.ok(gameService.getAllGames());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GameModel> getGameById(@PathVariable String id) {
        return gameService.getGameById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/publisher/{publisherId}")
    public ResponseEntity<List<GameModel>> getGamesByPublisher(@PathVariable String publisherId) {
        return ResponseEntity.ok(gameService.getGamesByPublisher(publisherId));
    }

    @PostMapping("/publish")
    public ResponseEntity<GameModel> createGame(@RequestBody GameModel gameModel) {
        Optional<Publisher> publisher = publisherRepository.findById(gameModel.getPublisherId());
        if (!publisher.isPresent()) {
            return ResponseEntity.badRequest().build();
        }
        Game game = gameMapper.fromDTO(gameModel, publisher.get());
        return ResponseEntity.ok(gameService.createGame(gameModel.getPublisherId(), game));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GameModel> updateGame(@PathVariable String id, @RequestBody GameModel gameModel) {
        GameModel existingGame = gameService.getGameById(id)
            .orElseThrow(() -> new IllegalArgumentException("Jeu introuvable avec l'ID: " + id));
        Optional<Publisher> publisher = publisherRepository.findById(existingGame.getPublisherId());
        if (!publisher.isPresent()) {
            return ResponseEntity.badRequest().build();
        }
        Game game = gameMapper.fromDTO(gameModel, publisher.get());
        game.setId(id);
        return ResponseEntity.ok(gameService.updateGame(id, game));
    }
}
