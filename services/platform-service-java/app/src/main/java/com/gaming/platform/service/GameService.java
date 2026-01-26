package com.gaming.platform.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.gaming.api.models.GameModel;
import com.gaming.platform.model.Game;
import com.gaming.platform.repository.GameRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameService {
    private final GameRepository gameRepository;

    public List<GameModel> getAllAvailableGames() {
        List<GameModel> games =  gameRepository.findAll().stream()
                .map(this::toGameModel)
                .collect(Collectors.toList());
        log.info("Retrieved {} available games", games.size());
        return games;
    }

    public Optional<GameModel> getGameById(String gameId) {
        return gameRepository.findById(gameId).map(this::toGameModel);
    }

    public List<GameModel> searchGames(String title) {
        return gameRepository.findByTitleContainingIgnoreCase(title)
                .stream()
                .map(this::toGameModel)
                .collect(Collectors.toList());
    }

    public List<GameModel> getGamesByGenre(String genre) {
        return gameRepository.findByGenre(genre)
                .stream()
                .map(this::toGameModel)
                .collect(Collectors.toList());
    }

    public List<GameModel> getGamesByPlatform(String platform) {
        return gameRepository.findByPlatform(platform)
                .stream()
                .map(this::toGameModel)
                .collect(Collectors.toList());
    }

    private GameModel toGameModel(Game game) {
        return GameModel.newBuilder()
                .setGameId(game.getGameId())
                .setTitle(game.getTitle())
                .setPublisherName(game.getPublisher())
                .setPublisherId("unknown") // Publisher ID not stored in Game entity
                .setPlatform(game.getPlatform())
                .setGenre(game.getGenre())
                .setReleaseTimeStamp(game.getReleaseTimeStamp())
                .setPrice(game.getPrice().doubleValue())
                .setVersion(game.getVersion())
                .setDescription(game.getDescription())
                .build();
    }

}
