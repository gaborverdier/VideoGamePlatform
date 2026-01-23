package org.example.services;

import org.example.controllers.PlayerDashboardController;
import org.example.models.Player;

public class SessionManager {
    private static SessionManager instance;
    private Player currentPlayer;
    private org.example.controllers.PlayerDashboardController playerController;
    
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

    public PlayerDashboardController getPlayerController() {
        return playerController;
    }

    public void setPlayerController(PlayerDashboardController controller) {
        this.playerController = controller;
    }
    
    public boolean isLoggedIn() {
        return currentPlayer != null;
    }
}