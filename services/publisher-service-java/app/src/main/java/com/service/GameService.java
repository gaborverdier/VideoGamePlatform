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
        return publisherRepository.findById(publisherId)
                .map(Publisher::getGames)
                .orElse(List.of());
    }

    public Game createGame(Long publisherId, Game game) {
        return publisherRepository.findById(publisherId).map(publisher -> {
            game.setPublisher(publisher);
            return gameRepository.save(game);
        }).orElse(null);
    }

    public Game updateGame(Long id, Game gameDetails) {
        return gameRepository.findById(id).map(game -> {
            game.setTitle(gameDetails.getTitle());
            game.setGenre(gameDetails.getGenre());
            game.setPlatform(gameDetails.getPlatform());
            return gameRepository.save(game);
        }).orElse(null);
    }

    public void deleteGame(Long id) {
        gameRepository.deleteById(id);
    }
}
