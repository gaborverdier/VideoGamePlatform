package com.vgp.client.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PurchaseResponse {
    
    @JsonProperty("purchaseId")
    private Integer purchaseId;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("message")
    private String message;
    
    public PurchaseResponse() {}
    
    public PurchaseResponse(Integer purchaseId, String status, String message) {
        this.purchaseId = purchaseId;
        this.status = status;
        this.message = message;
    }
    
    // Getters and setters
    public Integer getPurchaseId() {
        return purchaseId;
    }
    
    public void setPurchaseId(Integer purchaseId) {
        this.purchaseId = purchaseId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}
