package com.controller;

import com.gaming.api.models.GameModel;
import com.gaming.api.requests.GameReleased;
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
    public ResponseEntity<?> createGame(@RequestBody GameReleased gameModel) {
        
        Game game = gameMapper.fromReleaseModel(gameModel);
        return ResponseEntity.ok(gameService.createGame(gameModel.getPublisherId(), game));
    }

    @PutMapping("/update")
    public ResponseEntity<GameModel> updateGame(@RequestBody GameModel gameModel) {
        Game game = gameMapper.fromDTO(gameModel);
        return ResponseEntity.ok(gameService.updateGame(game));
    }
}
