package org.example.models;

import java.time.LocalDateTime;

public class Review {
    private String id;
    private String gameId;
    private String authorId;
    private String authorName;
    private int rating; // 1-5 étoiles
    private String comment;
    private LocalDateTime createdAt;
    private int helpfulCount = 0;
    private int notHelpfulCount = 0;
    private int authorPlaytime; // temps de jeu de l'auteur au moment de l'avis
    
    public Review(String gameId, String authorId, String authorName, int rating, String comment, int authorPlaytime) {
        this.id = "REV-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
        this.gameId = gameId;
        this.authorId = authorId;
        this. authorName = authorName;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = LocalDateTime.now();
        this.authorPlaytime = authorPlaytime;
    }
    
    // Getters
    public String getId() { return id; }
    public String getGameId() { return gameId; }
    public String getAuthorId() { return authorId; }
    public String getAuthorName() { return authorName; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public int getHelpfulCount() { return helpfulCount; }
    public int getNotHelpfulCount() { return notHelpfulCount; }
    public int getAuthorPlaytime() { return authorPlaytime; }
    
    // Méthodes
    public void markHelpful() {
        helpfulCount++;
    }
    
    public void markNotHelpful() {
        notHelpfulCount++;
    }
    
    public String getStars() {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < rating; i++) {
            stars.append("★");
        }
        for (int i = rating; i < 5; i++) {
            stars.append("☆");
        }
        return stars.toString();
    }
    
    public String getFormattedPlaytime() {
        if (authorPlaytime < 60) {
            return authorPlaytime + " min";
        }
        return (authorPlaytime / 60) + "h " + (authorPlaytime % 60) + "min";
    }
}