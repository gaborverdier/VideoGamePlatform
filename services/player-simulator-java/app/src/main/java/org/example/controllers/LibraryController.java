package org.example.controllers;

import org.example.services.PlatformApiClient;

public class LibraryController {

    private final PlatformApiClient platformApi;

    public LibraryController(PlatformApiClient platformApi) {
        this.platformApi = platformApi;
    }

    // Charger tous les jeux disponibles
    public String loadAllGames() {
        try {
            return platformApi.getAllGamesJson();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load games", e);
        }
    }

    // Rechercher par titre
    public String searchGames(String title) {
        try {
            return platformApi.searchGamesByTitleJson(title);
        } catch (Exception e) {
            throw new RuntimeException("Failed to search games", e);
        }
    }

    // Filtrer par genre
    public String getGamesByGenre(String genre) {
        try {
            return platformApi.getGamesByGenreJson(genre);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get games by genre", e);
        }
    }

    // Filtrer par plateforme
    public String getGamesByPlatform(String platform) {
        try {
            return platformApi.getGamesByPlatformJson(platform);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get games by platform", e);
        }
    }

    // Détails d’un jeu
    public String getGameDetails(String gameId) {
        try {
            return platformApi.getGameByIdJson(gameId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get game details", e);
        }
    }
}
