package com.vgp.shared.entity;

import jakarta.persistence.*;

@Entity
public class Editor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    public Editor() {}

    public Editor(String name) {
        this.name = name;
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
