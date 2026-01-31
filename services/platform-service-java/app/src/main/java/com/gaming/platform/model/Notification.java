package com.gaming.platform.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String NotificationId;

    @Column(nullable = false)
    private String userId;

    @Column
    private String type; // GAME_UPDATE, GAME_DLC, PRICE_DROP, NEW_REVIEW
    
    @Column
    private String gameId;
    
    @Column
    private String gameName;
    
    @Column
    private String title;

    @Column(nullable = false)
    private String description;
    
    @Column(nullable = false)
    private Instant timestamp;

}
