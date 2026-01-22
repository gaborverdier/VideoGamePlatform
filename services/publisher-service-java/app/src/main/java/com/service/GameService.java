package com.service;

import com.model.Game;
import com.model.Publisher;
import com.repository.GameRepository;
import com.repository.PublisherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GameService {
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private PublisherRepository publisherRepository;

    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }

    public Optional<Game> getGameById(Long id) {
        return gameRepository.findById(id);
    }

    public List<Game> getGamesByPublisher(Long publisherId) {
        Publisher publisher = publisherRepository.findById(publisherId).orElse(null);
        if (publisher == null) {
            return List.of();
        }
        return publisher.getGames();
    }

    public Game createGame(Long publisherId, Game game) {
        // Validation métier : le publisher doit exister
        Publisher publisher = publisherRepository.findById(publisherId)
            .orElseThrow(() -> new IllegalArgumentException("Publisher introuvable avec l'ID: " + publisherId));
        
        game.setPublisher(publisher);
        return gameRepository.save(game);
    }

    public Game updateGame(Long id, Game gameDetails) {
        // Validation métier : le jeu doit exister
        Game game = gameRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Jeu introuvable avec l'ID: " + id));
        
        game.setTitle(gameDetails.getTitle());
        game.setGenre(gameDetails.getGenre());
        game.setPlatform(gameDetails.getPlatform());
        
        return gameRepository.save(game);
    }

    public void deleteGame(Long id) {
        // Validation métier : le jeu doit exister
        if (!gameRepository.existsById(id)) {
            throw new IllegalArgumentException("Jeu introuvable avec l'ID: " + id);
        }
        gameRepository.deleteById(id);
    }
}
