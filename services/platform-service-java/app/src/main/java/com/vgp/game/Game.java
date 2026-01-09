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
    public Integer getId(){
        return id;
    }
    
    public String getName() {
        return name;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}