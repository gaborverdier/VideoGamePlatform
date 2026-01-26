package org.example.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.example.models.Game;
import org.example.models.Review;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaming.api.models.GameModel;

import javafx.application.Platform;
import javafx.scene.control.Alert;

public class GameDataService {
    private static GameDataService instance;
    private List<Game> allGames;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private GameDataService() {
        this.allGames = new ArrayList<>();
        loadFromBackend();
    }

    public static GameDataService getInstance() {
        if (instance == null) {
            instance = new GameDataService();
        }
        return instance;
    }

    private void loadFromBackend() {
        PlatformApiClient apiClient = new PlatformApiClient();
        try {
            String gamesJson = apiClient.getAllGamesJson();
            List<GameModel> avroGames = objectMapper.readValue(gamesJson, new TypeReference<List<GameModel>>() {});
            List<Game> loaded = new ArrayList<>();
            for (GameModel avro : avroGames) {
                Game g = Game.fromAvroModel(avro);
                // fetch reviews for this game and map into local Review model
                try {
                    String reviewsJson = apiClient.getReviewsForGameJson(g.getId());
                    if (reviewsJson != null && !reviewsJson.isEmpty()) {
                        List<Map<String, Object>> remoteReviews = objectMapper.readValue(reviewsJson, new TypeReference<List<Map<String, Object>>>(){});
                        for (Map<String, Object> r : remoteReviews) {
                            String authorId = r.containsKey("userId") ? String.valueOf(r.get("userId")) : "";
                            String authorName = r.containsKey("username") ? String.valueOf(r.get("username")) : (r.containsKey("authorName") ? String.valueOf(r.get("authorName")) : "");
                            int rating = 0;
                            try { rating = r.get("rating") != null ? Integer.parseInt(String.valueOf(r.get("rating"))) : 0; } catch (Exception ex) {}
                            String comment = r.containsKey("comment") ? String.valueOf(r.get("comment")) : (r.containsKey("reviewText") ? String.valueOf(r.get("reviewText")) : "");
                            Review local = new Review(g.getId(), authorId, authorName, rating, comment, 0);
                            g.getReviews().add(local);
                        }
                    }
                } catch (Exception ex) {
                    // non-fatal: log and continue
                    System.err.println("Failed to load reviews for game " + g.getId() + ": " + ex.getMessage());
                }
                loaded.add(g);
            }
            this.allGames = loaded;
            // debug print loaded games
            for (Game g : loaded) {
                System.out.println("Loaded game: " + g.getName());
            }
            // debug
        } catch (Exception e) {
            showError("Failed to load games from backend", e);
            this.allGames = new ArrayList<>();
        }
    }

    private void showError(String message, Exception e) {
        e.printStackTrace();
        try {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Data Load Error");
                alert.setHeaderText(message);
                alert.setContentText(e.getMessage() != null ? e.getMessage() : e.toString());
                alert.showAndWait();
            });
        } catch (Exception ex) {
            // If JavaFX not initialized, just print stack trace
            ex.printStackTrace();
        }
    }

    public synchronized void reload() {
        loadFromBackend();
    }

    public List<Game> getAllGames() {
        return new ArrayList<>(allGames);
    }

    public Game findGameById(String id) {
        return allGames.stream()
            .filter(g -> g.getId().equals(id))
            .findFirst()
            .orElse(null);
    }

    public List<Game> getUserLibrary(String userId) {
        try {
            PlatformApiClient apiClient = new PlatformApiClient();
            String json = apiClient.getUserLibraryJson(userId);
            List<GameModel> avroGames = objectMapper.readValue(json, new TypeReference<List<GameModel>>() {});
            List<Game> result = new ArrayList<>();
            for (GameModel avro : avroGames) {
                result.add(Game.fromAvroModel(avro));
            }

            // get installed flags from local storage
            try {
                java.util.Set<String> installed = InstalledGamesStore.getInstance().getInstalledForUser(userId);
                for (Game g : result) {
                    if (installed.contains(g.getId())) g.setInstalled(true);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return result;
        } catch (Exception e) {
            showError("Failed to load user library", e);
            return new ArrayList<>();
        }
    }

    public void installGameForUser(String userId, String gameId) throws Exception {
        PlatformApiClient apiClient = new PlatformApiClient();
        apiClient.installGame(userId, gameId);
        // Also mark as installed in local store
        try {
            InstalledGamesStore.getInstance().markInstalled(userId, gameId);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void purchaseGameForUser(String userId, String gameId) throws Exception {
        PlatformApiClient apiClient = new PlatformApiClient();
        apiClient.purchaseGame(userId, gameId);
    }
}