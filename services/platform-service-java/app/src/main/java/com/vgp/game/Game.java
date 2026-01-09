package com.vgp.game;

import com.vgp.editor.Editor;
import jakarta.persistence.*;

@Entity
public class Game {

    @Id
    @GeneratedValue
    private Integer id;

    private String name;

    @ManyToOne
    private Editor editor;

    public Game() {}

    public Game(String name, Editor editor) {
        this.name = name;
        this.editor = editor;
    }

    // getters & setters
}