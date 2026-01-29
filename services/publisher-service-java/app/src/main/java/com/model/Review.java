package com.model;

import java.time.LocalDateTime;

public class Review {
    private String id;
    private String gameId;
    private String gameName;
    private String playerId;
    private String playerName;
    private int rating; // 1-5
    private Integer playtimeMinutes; // durée de jeu en minutes (optionnel)
    private String comment;
    private LocalDateTime timestamp;
    
    public Review(String id, String gameId, String gameName, String playerId, 
                 String playerName, int rating, Integer playtimeMinutes, String comment, LocalDateTime timestamp) {
        this.id = id;
        this.gameId = gameId;
        this.gameName = gameName;
        this.playerId = playerId;
        this.playerName = playerName;
        this.rating = rating;
        this.playtimeMinutes = playtimeMinutes;
        this.comment = comment;
        this.timestamp = timestamp;
    }

    // Getters
    public String getId() { return id; }
    public String getGameId() { return gameId; }
    public String getGameName() { return gameName; }
    public String getPlayerId() { return playerId; }
    public String getPlayerName() { return playerName; }
    public int getRating() { return rating; }
    public Integer getPlaytimeMinutes() { return playtimeMinutes; }
    public String getComment() { return comment; }
    public LocalDateTime getTimestamp() { return timestamp; }

    public String getRatingStars() {
        return "⭐".repeat(rating) + "☆".repeat(5 - rating);
    }
}
