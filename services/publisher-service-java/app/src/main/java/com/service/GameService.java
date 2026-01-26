package com.service;

import com.gaming.api.models.GameModel;
import com.mapper.GameMapper;
import com.model.Game;
import com.model.Publisher;
import com.repository.GameRepository;
import com.repository.PublisherRepository;
import com.producer.EventProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GameService {
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private PublisherRepository publisherRepository;
    @Autowired
    private GameMapper gameMapper;
    @Autowired
    private EventProducer eventProducer;


    public List<GameModel> getAllGames() {
        return gameRepository.findAll().stream()
            .map(gameMapper::toDTO)
            .collect(Collectors.toList());
    }

    public Optional<GameModel> getGameById(String id) {
        return gameRepository.findById(id)
            .map(gameMapper::toDTO);
    }

    public List<GameModel> getGamesByPublisher(String publisherId) {
        Publisher publisher = publisherRepository.findById(publisherId).orElse(null);
        if (publisher == null) {
            return List.of();
        }
        return publisher.getGames().stream()
            .map(gameMapper::toDTO)
            .collect(Collectors.toList());
    }

    public GameModel createGame(String publisherId, Game game) {
        // Validation métier : le publisher doit exister
        Publisher publisher = publisherRepository.findById(publisherId)
            .orElseThrow(() -> new IllegalArgumentException("Publisher introuvable avec l'ID: " + publisherId));
        
        game.setPublisher(publisher);
        Game saved = gameRepository.save(game);

        GameModel dto;
        try {
            dto = gameMapper.toDTO(saved);

            String key = String.valueOf(dto.getGameId());
            String topic = "game-released";
            eventProducer.send(topic, key, dto);
        } catch (Exception e) {
            throw new RuntimeException("Échec de la production de l'événement pour le jeu créé", e);
        }
        
        return dto;
    }

    public GameModel updateGame(String id, Game gameDetails) {
        // Validation métier : le jeu doit exister
        Game game = gameRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Jeu introuvable avec l'ID: " + id));
        
        game.setTitle(gameDetails.getTitle());
        game.setGenre(gameDetails.getGenre());
        game.setPlatform(gameDetails.getPlatform());
        
        Game updated = gameRepository.save(game);
        return gameMapper.toDTO(updated);
    }

    public void deleteGame(String id) {
        // Validation métier : le jeu doit exister
        if (!gameRepository.existsById(id)) {
            throw new IllegalArgumentException("Jeu introuvable avec l'ID: " + id);
        }
        gameRepository.deleteById(id);
    }
}
