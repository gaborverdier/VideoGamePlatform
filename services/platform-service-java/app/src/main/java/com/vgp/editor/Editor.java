package com.vgp.editor;

import jakarta.persistence.*;

@Entity
public class Editor {

    @Id
    @GeneratedValue
    private Integer id;

    private String name;

    public Editor() {}

    public Editor(String name) {
        this.name = name;
    }

    // getters & setters
}
