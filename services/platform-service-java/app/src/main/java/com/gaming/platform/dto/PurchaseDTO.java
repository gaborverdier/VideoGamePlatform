package com.gaming.platform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PurchaseDTO {
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    @NotBlank(message = "Game ID is required")
    private String gameId;
    
    private String paymentMethod;
    private String region;
}