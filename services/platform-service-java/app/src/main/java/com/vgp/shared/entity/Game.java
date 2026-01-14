package com.vgp.shared.entity;

import jakarta.persistence.*;

@Entity
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    
    public Editor getEditor() {
        return editor;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public void setEditor(Editor editor) {
        this.editor = editor;
    }
}
