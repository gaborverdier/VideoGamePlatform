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

import java.util.List;

@RestController
@RequestMapping("api/games")
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

    @PostMapping("/publisher/{publisherId}")
    public ResponseEntity<GameModel> createGame(@PathVariable("publisherId") String publisherId, @RequestBody GameModel gameModel) {
        Publisher publisher = publisherRepository.findById(publisherId)
            .orElseThrow(() -> new IllegalArgumentException("Publisher introuvable avec l'ID: " + publisherId));
        Game game = gameMapper.fromDTO(gameModel, publisher);
        return ResponseEntity.ok(gameService.createGame(publisherId, game));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GameModel> updateGame(@PathVariable String id, @RequestBody GameModel gameModel) {
        GameModel existingGame = gameService.getGameById(id)
            .orElseThrow(() -> new IllegalArgumentException("Jeu introuvable avec l'ID: " + id));
        Publisher publisher = publisherRepository.findById(existingGame.getPublisherId())
            .orElseThrow(() -> new IllegalArgumentException("Publisher introuvable"));
        Game game = gameMapper.fromDTO(gameModel, publisher);
        game.setId(id);
        return ResponseEntity.ok(gameService.updateGame(id, game));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGame(@PathVariable String id) {
        gameService.deleteGame(id);
        return ResponseEntity.noContent().build();
    }
}
