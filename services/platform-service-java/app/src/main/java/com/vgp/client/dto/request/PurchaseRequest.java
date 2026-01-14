package com.vgp.client.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

public class PurchaseRequest {
    
    @JsonProperty("userId")
    @NotNull(message = "User ID is required")
    private Integer userId;
    
    @JsonProperty("gameId")
    @NotNull(message = "Game ID is required")
    private Integer gameId;
    
    @JsonProperty("price")
    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price must be positive")
    private Double price;
    
    @JsonProperty("platform")
    @NotBlank(message = "Platform is required")
    private String platform;
    
    @JsonProperty("paymentMethod")
    private String paymentMethod;
    
    public PurchaseRequest() {}
    
    public PurchaseRequest(Integer userId, Integer gameId, Double price, String platform, String paymentMethod) {
        this.userId = userId;
        this.gameId = gameId;
        this.price = price;
        this.platform = platform;
        this.paymentMethod = paymentMethod;
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
    
    public Double getPrice() {
        return price;
    }
    
    public void setPrice(Double price) {
        this.price = price;
    }
    
    public String getPlatform() {
        return platform;
    }
    
    public void setPlatform(String platform) {
        this.platform = platform;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
