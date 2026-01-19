package org.example.services;

import org.example.models.Player;

public class SessionManager {
    private static SessionManager instance;
    private Player currentPlayer;
    
    private SessionManager() {}
    
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    public void login(Player player) {
        this.currentPlayer = player;
    }
    
    public void logout() {
        this.currentPlayer = null;
    }
    
    public Player getCurrentPlayer() {
        return currentPlayer;
    }
    
    public boolean isLoggedIn() {
        return currentPlayer != null;
    }
}