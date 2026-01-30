package com.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DLC {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;
    private Long releaseTimeStamp;
    private String description;
    private Double price;

    @ManyToOne
    @JoinColumn(name = "game_id")
    private Game game;
}
