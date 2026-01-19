package org.example.models;

import java.util.ArrayList;
import java.util.List;

public class Library {
    private final List<Game> games;

    public Library() {
        this.games = new ArrayList<>();
    }

    public void addGame(Game game) {
        if (!hasGame(game)) {
            games.add(game);
        }
    }

    public boolean hasGame(Game game) {
        return games.contains(game);  //Is this object already inside my collection?
    }

    public List<Game> getGames() {
        return new ArrayList<>(games);
    }

    public Game getRandomGame() {
        if (games.isEmpty())
            return null;
        return games.get((int) (Math.random() * games.size()));
    }
}
