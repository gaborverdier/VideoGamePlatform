package org.example.old;

import javafx.scene.image.Image;

public class Game {
    private String name;
    private double price;
    private String genre;
    private Image coverImage;

    public Game(String name, double price, String genre, Image coverImage) {
        this.name = name;
        this.price = price;
        this.genre = genre;
        this.coverImage = coverImage;
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

    public Image getCoverImage() {
        return coverImage;
    }
}