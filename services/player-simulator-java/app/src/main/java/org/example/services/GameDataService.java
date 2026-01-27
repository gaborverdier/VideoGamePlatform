package org.example.services;

import java.util.ArrayList;
import java.util.List;

import org.example.models.Game;
import org.example.models.Review;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaming.api.models.GameModel;

import com.gaming.events.GameReviewed;

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
            List<GameModel> avroGames = objectMapper.readValue(gamesJson, new TypeReference<List<GameModel>>() {
            });
            List<Game> loaded = new ArrayList<>();
            for (GameModel avro : avroGames) {
                Game g = Game.fromAvroModel(avro);
                // fetch reviews for this game and map into local Review model
                try {
                    String reviewsJson = apiClient.getReviewsForGameJson(g.getId());
                    System.out.println("\n\n\n\n\n\nFetched reviews JSON for game " + g.getId() + ": " + reviewsJson + "\n\n\n\n\n\n");
                    if (reviewsJson != null && !reviewsJson.isEmpty()) {
                        List<GameReviewed> remoteReviews = objectMapper.readValue(reviewsJson,
                                new TypeReference<List<GameReviewed>>() {
                                });
                        for (GameReviewed rm : remoteReviews) {
                            String authorId = rm.getUserId() != null ? rm.getUserId() : "";
                            String authorName = rm.getUsername() != null ? rm.getUsername() : "";
                            int rating = rm.getRating();
                            String comment = rm.getReviewText() != null ? rm.getReviewText() : "";
                            Review local = new Review(g.getId(), authorId, authorName, rating, comment, 0);
                            // set id and createdAt if available
                            try {
                                java.lang.reflect.Field idField = Review.class.getDeclaredField("id");
                                idField.setAccessible(true);
                                idField.set(local, rm.getReviewId() != null ? rm.getReviewId() : local.getId());
                            } catch (Exception ignore) {
                            }
                            try {
                                java.lang.reflect.Field createdAtField = Review.class.getDeclaredField("createdAt");
                                createdAtField.setAccessible(true);
                                long regTs = rm.getRegistrationTimestamp();
                                if (regTs > 0L)
                                    createdAtField.set(local, java.time.LocalDateTime.ofInstant(
                                            java.time.Instant.ofEpochMilli(regTs), java.time.ZoneId.systemDefault()));
                            } catch (Exception ignore) {
                            }
                            g.getReviews().add(local);
                        }
                    }
                } catch (Exception ex) {
                    // non-fatal: log and continue
                    System.err.println("Failed to load reviews for game " + g.getId() + ": " + ex.getMessage());
                }
                // print number of retrieved comments for this game
                try {
                    long commentCount = g.getReviews().stream()
                            .filter(r -> r.getComment() != null && !r.getComment().isEmpty()).count();
                    System.out.println(
                            "Game '" + g.getName() + "' (" + g.getId() + ") - retrieved comments: " + commentCount);
                } catch (Exception ex) {
                    // defensive: don't let logging break loading
                    System.err.println("Failed to count comments for game " + g.getId() + ": " + ex.getMessage());
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
            List<GameModel> avroGames = objectMapper.readValue(json, new TypeReference<List<GameModel>>() {
            });
            List<Game> result = new ArrayList<>();
            for (GameModel avro : avroGames) {
                result.add(Game.fromAvroModel(avro));
            }

            // get installed flags from local storage
            try {
                java.util.Set<String> installed = InstalledGamesStore.getInstance().getInstalledForUser(userId);
                for (Game g : result) {
                    if (installed.contains(g.getId()))
                        g.setInstalled(true);
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