package com.gaming.events;

/**
 * Stub class for GameRatingSubmittedEvent
 * This is a temporary mock until Kafka dependencies can be resolved
 * In production, this will be generated from Avro schema
 */
public class GameRatingSubmittedEvent {
    private String ratingId;
    private String userId;
    private String gameId;
    private Integer rating;
    private String review;
    private Long submittedTimestamp;
    
    private GameRatingSubmittedEvent(Builder builder) {
        this.ratingId = builder.ratingId;
        this.userId = builder.userId;
        this.gameId = builder.gameId;
        this.rating = builder.rating;
        this.review = builder.review;
        this.submittedTimestamp = builder.submittedTimestamp;
    }
    
    public static Builder newBuilder() {
        return new Builder();
    }
    
    public String getRatingId() { return ratingId; }
    public String getUserId() { return userId; }
    public String getGameId() { return gameId; }
    public Integer getRating() { return rating; }
    public String getReview() { return review; }
    public Long getSubmittedTimestamp() { return submittedTimestamp; }
    
    public static class Builder {
        private String ratingId;
        private String userId;
        private String gameId;
        private Integer rating;
        private String review;
        private Long submittedTimestamp;
        
        public Builder setRatingId(String ratingId) {
            this.ratingId = ratingId;
            return this;
        }
        
        public Builder setUserId(String userId) {
            this.userId = userId;
            return this;
        }
        
        public Builder setGameId(String gameId) {
            this.gameId = gameId;
            return this;
        }
        
        public Builder setRating(Integer rating) {
            this.rating = rating;
            return this;
        }
        
        public Builder setReview(String review) {
            this.review = review;
            return this;
        }
        
        public Builder setSubmittedTimestamp(Long submittedTimestamp) {
            this.submittedTimestamp = submittedTimestamp;
            return this;
        }
        
        public GameRatingSubmittedEvent build() {
            return new GameRatingSubmittedEvent(this);
        }
    }
}
