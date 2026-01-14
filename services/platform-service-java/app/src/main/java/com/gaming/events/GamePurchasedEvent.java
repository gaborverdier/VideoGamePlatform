package com.gaming.events;

/**
 * Stub class for GamePurchasedEvent
 * This is a temporary mock until Kafka dependencies can be resolved
 * In production, this will be generated from Avro schema
 */
public class GamePurchasedEvent {
    private String purchaseId;
    private String userId;
    private String gameId;
    private String gameTitle;
    private Double price;
    private String currency;
    private String platform;
    private Long purchaseTimestamp;
    private String paymentMethod;
    
    private GamePurchasedEvent(Builder builder) {
        this.purchaseId = builder.purchaseId;
        this.userId = builder.userId;
        this.gameId = builder.gameId;
        this.gameTitle = builder.gameTitle;
        this.price = builder.price;
        this.currency = builder.currency;
        this.platform = builder.platform;
        this.purchaseTimestamp = builder.purchaseTimestamp;
        this.paymentMethod = builder.paymentMethod;
    }
    
    public static Builder newBuilder() {
        return new Builder();
    }
    
    public String getPurchaseId() { return purchaseId; }
    public String getUserId() { return userId; }
    public String getGameId() { return gameId; }
    public String getGameTitle() { return gameTitle; }
    public Double getPrice() { return price; }
    public String getCurrency() { return currency; }
    public String getPlatform() { return platform; }
    public Long getPurchaseTimestamp() { return purchaseTimestamp; }
    public String getPaymentMethod() { return paymentMethod; }
    
    public static class Builder {
        private String purchaseId;
        private String userId;
        private String gameId;
        private String gameTitle;
        private Double price;
        private String currency;
        private String platform;
        private Long purchaseTimestamp;
        private String paymentMethod;
        
        public Builder setPurchaseId(String purchaseId) {
            this.purchaseId = purchaseId;
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
        
        public Builder setGameTitle(String gameTitle) {
            this.gameTitle = gameTitle;
            return this;
        }
        
        public Builder setPrice(Double price) {
            this.price = price;
            return this;
        }
        
        public Builder setCurrency(String currency) {
            this.currency = currency;
            return this;
        }
        
        public Builder setPlatform(String platform) {
            this.platform = platform;
            return this;
        }
        
        public Builder setPurchaseTimestamp(Long purchaseTimestamp) {
            this.purchaseTimestamp = purchaseTimestamp;
            return this;
        }
        
        public Builder setPaymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
            return this;
        }
        
        public GamePurchasedEvent build() {
            return new GamePurchasedEvent(this);
        }
    }
}
