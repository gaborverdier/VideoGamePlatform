package com.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Crash {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;
    private String crashDate;
    private String gameVersion;

    @ManyToOne
    @JoinColumn(name = "game_id")
    private Game game;
}
