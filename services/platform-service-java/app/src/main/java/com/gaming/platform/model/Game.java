package com.gaming.platform.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "games")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String gameId;

    @Column(nullable = false)
    private String title;

    @Column
    private String publisher;

    @Column
    private String platform;

    @Column
    private String genre;

    @Column
    private Long releaseTimeStamp;

    @Column(nullable = false)
    private BigDecimal price;

    @Column
    private String version;

    @Column
    private LocalDateTime lastUpdated;

    @Column(length = 1000)
    private String description;
}