package org.example.services;

import java.util.ArrayList;
import java.util.List;
import org.example.models.Game;
import com.gaming.api.models.GameModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

public class GameDataService {
    private static GameDataService instance;
    private List<Game> allGames;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private GameDataService() {
        this.allGames = new ArrayList<>();
        PlatformApiClient apiClient = new PlatformApiClient();
        try {
            String gamesJson = apiClient.getAllGamesJson();
            List<GameModel> avroGames = objectMapper.readValue(gamesJson, new TypeReference<List<GameModel>>() {});
            this.allGames = new ArrayList<>();
            for (GameModel avro : avroGames) {
                this.allGames.add(Game.fromAvroModel(avro));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static GameDataService getInstance() {
        if (instance == null) {
            instance = new GameDataService();
        }
        return instance;
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
}