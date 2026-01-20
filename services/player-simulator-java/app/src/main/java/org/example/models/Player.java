package org.example.models;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Player {
    private String id;
    private String username;
    private String email;
    private double wallet;
    private List<Game> ownedGames;
    private List<String> friendIds;
    private List<String> followedPublisherIds;

    public enum PurchaseResult {
        SUCCESS,
        ALREADY_OWNED,
        INSUFFICIENT_FUNDS,
        UNSUPPORTED_PLATFORM
    }
    
    public Player(String id, String username, String email, double wallet) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.wallet = wallet;
        this.ownedGames = new ArrayList<>();
        this.friendIds = new ArrayList<>();
        this.followedPublisherIds = new ArrayList<>();
    }
    
    public Player(String username, String email) {
        this(generateId(), username, email, 100.0);
    }
    
    private static String generateId() {
        return "PLAYER-" + System.currentTimeMillis();
    }
    
    // Getters
    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public double getWallet() { return wallet; }
    public List<Game> getOwnedGames() { return new ArrayList<>(ownedGames); }
    public List<String> getFriendIds() { return new ArrayList<>(friendIds); }
    public List<String> getFollowedPublisherIds() { return new ArrayList<>(followedPublisherIds); }
    
    // Setters
    public void setWallet(double wallet) { this.wallet = wallet; }
    
    // Acheter un jeu sur une plateforme donnée
    public PurchaseResult purchaseGame(Game game, Platform platform) {
        if (game == null || platform == null) {
            return PurchaseResult.UNSUPPORTED_PLATFORM;
        }

        if (!game.getSupportedPlatforms().contains(platform)) {
            return PurchaseResult.UNSUPPORTED_PLATFORM;
        }

        if (game.isOwnedOnPlatform(platform)) {
            return PurchaseResult.ALREADY_OWNED;
        }

        if (wallet < game.getPrice()) {
            return PurchaseResult.INSUFFICIENT_FUNDS;
        }

        wallet -= game.getPrice();
        game.purchase(platform);
        if (!ownedGames.contains(game)) {
            ownedGames.add(game);
        }
        return PurchaseResult.SUCCESS;
    }
    
    public boolean ownsGame(String gameId) {
        return ownedGames.stream().anyMatch(game -> game.getId().equals(gameId));
    }
    
    public int getOwnedGamesCount() {
        return ownedGames.size();
    }
    
    public double getTotalSpent() {
        return ownedGames.stream().mapToDouble(Game::getPrice).sum();
    }
    
    public List<Game> getGamesByGenre(String genre) {
        return ownedGames.stream()
                .filter(game -> game.getGenre().equalsIgnoreCase(genre))
                .collect(Collectors.toList());
    }
    
    public void addFriend(String friendId) {
        if (!friendIds.contains(friendId)) {
            friendIds.add(friendId);
        }
    }

    public void removeFriend(String friendId) {
        friendIds.remove(friendId);
    }
    
    public void followPublisher(String publisherId) {
        if (!followedPublisherIds.contains(publisherId)) {
            followedPublisherIds.add(publisherId);
        }
    }
    
    public void unfollowPublisher(String publisherId) {
        followedPublisherIds.remove(publisherId);
    }
    
    @Override
    public String toString() {
        return String.format("Player{id='%s', username='%s', wallet=%.2f€, ownedGames=%d}", 
                           id, username, wallet, ownedGames. size());
    }
}