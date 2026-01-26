package com.model;

import java.util.Date;

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
    private Long releaseTime;
    private String description;

    @ManyToOne
    @JoinColumn(name = "game_id")
    private Game game;
}
