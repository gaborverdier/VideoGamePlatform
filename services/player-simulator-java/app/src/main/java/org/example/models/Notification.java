package org.example.models;

import java.time.LocalDateTime;

public class Notification {
    public enum Type {
        GAME_UPDATE,
        GAME_DLC,
        PRICE_DROP,
        NEW_REVIEW,
        REVIEW_HELPFUL,
        PUBLISHER_NEW_GAME,
        FRIEND_ACTIVITY
    }
    
    private String id;
    private Type type;
    private String title;
    private String message;
    private LocalDateTime createdAt;
    private boolean isRead;
    private boolean isFromFavorite;
    private String relatedGameId;
    
    public Notification(Type type, String title, String message, boolean isFromFavorite, String relatedGameId) {
        this.id = "NOTIF-" + System.currentTimeMillis();
        this.type = type;
        this. title = title;
        this. message = message;
        this. createdAt = LocalDateTime.now();
        this.isRead = false;
        this.isFromFavorite = isFromFavorite;
        this.relatedGameId = relatedGameId;
    }
    
    public String getId() { return id; }
    public Type getType() { return type; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isRead() { return isRead; }
    public boolean isFromFavorite() { return isFromFavorite; }
    public String getRelatedGameId() { return relatedGameId; }
    
    public void markAsRead() {
        this.isRead = true;
    }
    
    public String getIcon() {
        switch (type) {
            case GAME_UPDATE:  return "⬇";
            case GAME_DLC: return "🎁";
            case PRICE_DROP: return "💰";
            case NEW_REVIEW: return "⭐";
            case REVIEW_HELPFUL: return "👍";
            case PUBLISHER_NEW_GAME: return "🎮";
            case FRIEND_ACTIVITY: return "👥";
            default: return "📢";
        }
    }
}