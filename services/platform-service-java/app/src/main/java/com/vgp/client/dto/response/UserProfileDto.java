package com.vgp.client.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class UserProfileDto {
    
    @JsonProperty("userId")
    private Integer userId;
    
    @JsonProperty("username")
    private String username;
    
    @JsonProperty("purchasedGames")
    private List<GameDto> purchasedGames;
    
    public UserProfileDto() {}
    
    public UserProfileDto(Integer userId, String username, List<GameDto> purchasedGames) {
        this.userId = userId;
        this.username = username;
        this.purchasedGames = purchasedGames;
    }
    
    // Getters and setters
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public List<GameDto> getPurchasedGames() {
        return purchasedGames;
    }
    
    public void setPurchasedGames(List<GameDto> purchasedGames) {
        this.purchasedGames = purchasedGames;
    }
}
