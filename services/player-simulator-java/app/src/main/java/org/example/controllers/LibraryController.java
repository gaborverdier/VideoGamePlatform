package org.example.controllers;

import org.example.services.PlatformApiClient;
import com.gaming.api.models.GameModel;
import org.example.models.Game;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import java.util.stream.Collectors;

public class LibraryController {

    private final PlatformApiClient platformApi;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LibraryController(PlatformApiClient platformApi) {
        this.platformApi = platformApi;
    }

    // Charger tous les jeux disponibles
    public List<Game> loadAllGames() {
        try {
            String json = platformApi.getAllGamesJson();
            List<GameModel> avroGames = objectMapper.readValue(json, new TypeReference<List<GameModel>>() {});
            return avroGames.stream().map(Game::fromAvroModel).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to load games", e);
        }
    }

    // Rechercher par titre
    public List<Game> searchGames(String title) {
        try {
            String json = platformApi.searchGamesByTitleJson(title);
            List<GameModel> avroGames = objectMapper.readValue(json, new TypeReference<List<GameModel>>() {});
            return avroGames.stream().map(Game::fromAvroModel).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to search games", e);
        }
    }

    // Filtrer par genre
    public List<Game> getGamesByGenre(String genre) {
        try {
            String json = platformApi.getGamesByGenreJson(genre);
            List<GameModel> avroGames = objectMapper.readValue(json, new TypeReference<List<GameModel>>() {});
            return avroGames.stream().map(Game::fromAvroModel).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to get games by genre", e);
        }
    }

    // Filtrer par plateforme
    public List<Game> getGamesByPlatform(String platform) {
        try {
            String json = platformApi.getGamesByPlatformJson(platform);
            List<GameModel> avroGames = objectMapper.readValue(json, new TypeReference<List<GameModel>>() {});
            return avroGames.stream().map(Game::fromAvroModel).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to get games by platform", e);
        }
    }

    // Détails d’un jeu
    public Game getGameDetails(String gameId) {
        try {
            String json = platformApi.getGameByIdJson(gameId);
            GameModel avroGame = objectMapper.readValue(json, GameModel.class);
            return Game.fromAvroModel(avroGame);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get game details", e);
        }
    }
}
