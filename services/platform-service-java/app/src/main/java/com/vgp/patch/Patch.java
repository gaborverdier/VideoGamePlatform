package com.vgp.patch;

import com.vgp.game.*;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class Patch {

    @Id
    @GeneratedValue
    private Integer id;

    @ManyToOne
    private Game game;

    private String version;

    @Column(length = 2000)
    private String content;

    private Instant releasedAt = Instant.now();

    public Patch() {}

    public Patch(Game game, String version, String content) {
        this.game = game;
        this.version = version;
        this.content = content;
    }

    // getters & setters
}
