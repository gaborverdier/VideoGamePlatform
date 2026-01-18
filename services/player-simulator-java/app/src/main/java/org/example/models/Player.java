package org.example.models;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private String id;
    private String username;
    private double wallet; // Solde du joueur
    private List<Game> ownedGames; // Jeux possédés
    
    public Player(String id, String username, double wallet) {
        this.id = id;
        this. username = username;
        this. wallet = wallet;
        this. ownedGames = new ArrayList<>();
    }
    
    public Player(String username) {
        this(generateId(), username, 100.0); // 100€ par défaut
    }
    
    private static String generateId() {
        return "PLAYER-" + System.currentTimeMillis();
    }
    
    // Getters
    public String getId() {
        return id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public double getWallet() {
        return wallet;
    }
    
    public List<Game> getOwnedGames() {
        return new ArrayList<>(ownedGames); // Copie défensive
    }
    
    // Setters
    public void setWallet(double wallet) {
        this.wallet = wallet;
    }
    
    // Acheter un jeu
    public boolean purchaseGame(Game game) {
        if (game.isOwned()) {
            System.out.println("Jeu déjà possédé !");
            return false;
        }
        
        if (wallet < game.getPrice()) {
            System.out.println("Solde insuffisant !");
            return false;
        }
        
        wallet -= game.getPrice();
        game.purchase();
        ownedGames.add(game);
        System.out.println("Jeu acheté : " + game.getName());
        return true;
    }
    
    // Vérifier si le joueur possède un jeu
    public boolean ownsGame(String gameId) {
        return ownedGames.stream()
                .anyMatch(game -> game.getId().equals(gameId));
    }
    
    // Obtenir le nombre de jeux possédés
    public int getOwnedGamesCount() {
        return ownedGames.size();
    }
    
    @Override
    public String toString() {
        return String.format("Player{id='%s', username='%s', wallet=%.2f€, ownedGames=%d}", 
                           id, username, wallet, ownedGames.size());
    }
}