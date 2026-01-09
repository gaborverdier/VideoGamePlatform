package com.vgp.user;

import jakarta.persistence.*;

@Entity
@Table(name = "app_user")
public class User {

    @Id
    @GeneratedValue
    private Integer id;

    private String username;

    public User() {}

    public User(String username) {
        this.username = username;
    }

    // getters & setters
}
