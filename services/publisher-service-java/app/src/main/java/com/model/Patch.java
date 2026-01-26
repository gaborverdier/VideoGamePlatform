package com.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Patch {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String version;
    private Long releaseTimeStamp;
    private String description;

    @ManyToOne
    @JoinColumn(name = "game_id")
    private Game game;
}
