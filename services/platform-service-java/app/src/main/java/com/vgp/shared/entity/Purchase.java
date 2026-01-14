package com.vgp.shared.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "purchases")
public class Purchase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;
    
    private Double price;
    private String currency;
    private String platform;
    private String paymentMethod;
    
    @Column(name = "purchase_timestamp")
    private Instant purchaseTimestamp;
    
    public Purchase() {}
    
    public Purchase(User user, Game game, Double price, String currency, String platform, String paymentMethod) {
        this.user = user;
        this.game = game;
        this.price = price;
        this.currency = currency;
        this.platform = platform;
        this.paymentMethod = paymentMethod;
    }
    
    @PrePersist
    protected void onCreate() {
        if (purchaseTimestamp == null) {
            purchaseTimestamp = Instant.now();
        }
    }
    
    // Getters and setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public Game getGame() {
        return game;
    }
    
    public void setGame(Game game) {
        this.game = game;
    }
    
    public Double getPrice() {
        return price;
    }
    
    public void setPrice(Double price) {
        this.price = price;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
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
    
    public Instant getPurchaseTimestamp() {
        return purchaseTimestamp;
    }
    
    public void setPurchaseTimestamp(Instant purchaseTimestamp) {
        this.purchaseTimestamp = purchaseTimestamp;
    }
}
