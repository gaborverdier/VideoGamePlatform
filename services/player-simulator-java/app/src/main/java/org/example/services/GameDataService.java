package org.example.services;

import java.util.ArrayList;
import java.util.List;
import org.example.models.Game;
import com.gaming.api.models.GameModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
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
                loaded.add(Game.fromAvroModel(avro));
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
            return result;
        } catch (Exception e) {
            showError("Failed to load user library", e);
            return new ArrayList<>();
        }
    }

    public void installGameForUser(String userId, String gameId) throws Exception {
        PlatformApiClient apiClient = new PlatformApiClient();
        apiClient.installGame(userId, gameId);
    }

    public void purchaseGameForUser(String userId, String gameId) throws Exception {
        PlatformApiClient apiClient = new PlatformApiClient();
        apiClient.purchaseGame(userId, gameId);
    }
}