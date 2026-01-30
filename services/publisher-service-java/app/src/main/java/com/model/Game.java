package com.model;

import jakarta.persistence.*;
import java.util.List;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String title;
    private String genre;
    private String platform;
    private Long releaseTimeStamp;
    private Double price;
    private String version;
    private String description;


    @ManyToOne
    @JoinColumn(name = "publisher_id")
    private Publisher publisher;

    @OneToMany(mappedBy = "game")
    private List<CrashAggregation> crashes;

    @OneToMany(mappedBy = "game")
    private List<Patch> patches;

    @OneToMany(mappedBy = "game")
    private List<DLC> dlcs;
}
