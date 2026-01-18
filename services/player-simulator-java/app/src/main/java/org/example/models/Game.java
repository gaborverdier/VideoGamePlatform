package org.example.models;

import javafx.scene.image.Image;

public class Game {
    private String id;
    private String name;
    private double price;
    private String genre;
    private String publisher;
    private String coverImageUrl;
    private Image coverImage;
    private boolean owned;
    private String description;
    private double rating; // Note sur 5
    private int playtime; // Temps de jeu en heures
    
    // Constructeur complet
    public Game(String id, String name, double price, String genre, String publisher, 
                String coverImageUrl, String description, double rating, int playtime) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.genre = genre;
        this.publisher = publisher;
        this.coverImageUrl = coverImageUrl;
        this.coverImage = new Image(coverImageUrl, true);
        this.owned = false;
        this.description = description;
        this.rating = rating;
        this.playtime = playtime;
    }
    
    // Constructeur simplifié
    public Game(String name, double price, String genre, String coverImageUrl, 
                String description, double rating, int playtime) {
        this(generateId(), name, price, genre, "Unknown Publisher", coverImageUrl, 
             description, rating, playtime);
    }
    
    private static String generateId() {
        return "GAME-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
    }
    
    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getGenre() { return genre; }
    public String getPublisher() { return publisher; }
    public String getCoverImageUrl() { return coverImageUrl; }
    public Image getCoverImage() { return coverImage; }
    public boolean isOwned() { return owned; }
    public String getDescription() { return description; }
    public double getRating() { return rating; }
    public int getPlaytime() { return playtime; }
    
    // Setters
    public void setOwned(boolean owned) { this.owned = owned; }
    public void setPrice(double price) { this.price = price; }
    
    public void purchase() {
        this.owned = true;
    }
    
    public String getFormattedPrice() {
        if (price == 0.0) {
            return "GRATUIT";
        }
        return String.format("%.2f€", price);
    }
    
    @Override
    public String toString() {
        return String.format("Game{id='%s', name='%s', price=%.2f, genre='%s', owned=%s}", 
                           id, name, price, genre, owned);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Game game = (Game) o;
        return id.equals(game.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}