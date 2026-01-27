package com.model;

import java.security.Timestamp;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Crash {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private Long crashTimeStamp;
    private String gameVersion;

    private String platform;
    private String errorMessage;

    @ManyToOne
    @JoinColumn(name = "game_id")
    private Game game;

    
}
