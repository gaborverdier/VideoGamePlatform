package org.example.controllers;

import java.util.HashSet;
import java.util.Set;

public class MyGamesController {

    // bibliothèque locale du joueur (IDs de jeux)
    private final Set<String> ownedGames = new HashSet<>();

    // ajouter un jeu après achat
    public void addGameToLibrary(String gameId) {
        ownedGames.add(gameId);
    }

    // vérifier si le joueur possède un jeu
    public boolean ownsGame(String gameId) {
        return ownedGames.contains(gameId);
    }

    // récupérer la bibliothèque
    public Set<String> getMyGames() {
        return Set.copyOf(ownedGames);
    }

    // supprimer un jeu (optionnel)
    public void removeGame(String gameId) {
        ownedGames.remove(gameId);
    }
}