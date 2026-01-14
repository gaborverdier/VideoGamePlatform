package com.vgp.shared.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class Patch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private Game game;

    private String version;

    @Column(length = 2000)
    private String content;

    private Instant releasedAt;

    public Patch() {}

    public Patch(Game game, String version, String content) {
        this.game = game;
        this.version = version;
        this.content = content;
    }
    
    @PrePersist
    protected void onCreate() {
        if (releasedAt == null) {
            releasedAt = Instant.now();
        }
    }

    // getters & setters
    public Integer getId(){
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }

    public String getContent(){
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Instant getReleasedAt() {
        return releasedAt;
    }

    public void setReleasedAt(Instant releasedAt) {
        this.releasedAt = releasedAt;
    }
}
