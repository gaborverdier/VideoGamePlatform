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
