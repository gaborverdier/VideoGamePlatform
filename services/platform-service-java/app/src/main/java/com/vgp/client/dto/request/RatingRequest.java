package com.vgp.client.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

public class RatingRequest {
    
    @JsonProperty("userId")
    @NotNull(message = "User ID is required")
    private Integer userId;
    
    @JsonProperty("gameId")
    @NotNull(message = "Game ID is required")
    private Integer gameId;
    
    @JsonProperty("rating")
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer rating;
    
    @JsonProperty("review")
    private String review;
    
    public RatingRequest() {}
    
    public RatingRequest(Integer userId, Integer gameId, Integer rating, String review) {
        this.userId = userId;
        this.gameId = gameId;
        this.rating = rating;
        this.review = review;
    }
    
    // Getters and setters
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public Integer getGameId() {
        return gameId;
    }
    
    public void setGameId(Integer gameId) {
        this.gameId = gameId;
    }
    
    public Integer getRating() {
        return rating;
    }
    
    public void setRating(Integer rating) {
        this.rating = rating;
    }
    
    public String getReview() {
        return review;
    }
    
    public void setReview(String review) {
        this.review = review;
    }
}
