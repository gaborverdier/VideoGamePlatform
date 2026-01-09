package org;

public class Game {
    private String name;
    private double price;
    private String genre;

    public Game(String name, double price, String genre) {
        this.name = name;
        this.price = price;
        this.genre = genre;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getGenre() {
        return genre;
    }
}