package org.example.models;

import java.util.UUID;

public class Player {
    private final String id;
    private final String username;
    private final Library library;

    public Player(String username) {
        this.id = UUID.randomUUID().toString();
        this.username = username;
        this.library = new Library();
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public Library getLibrary() {
        return library;
    }

    public void purchaseGame(Game game) {
        library.addGame(game);
        System.out.println(username + " purchased " + game.getName());
    }

    public void playGame(Game game) {
        if (library.hasGame(game)) {
            System.out.println(username + " is playing " + game.getName());
            // Logic to potentially generate crash or rating events could go here
        } else {
            System.out.println(username + " does not own " + game.getName());
        }
    }
}
