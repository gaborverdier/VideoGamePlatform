package com.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DLC {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String releaseDate;
    private String description;

    @ManyToOne
    @JoinColumn(name = "game_id")
    private Game game;
}
