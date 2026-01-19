package org.example.models;

public class Game {
    private final String name;
    private final String platform;
    private final String genre;
    private final String publisher;
    private String version;

    public Game(String name, String platform, String genre, String publisher) {
        this.name = name;
        this.platform = platform;
        this.genre = genre;
        this.publisher = publisher;
        this.version = "1.0.0"; // Default version
    }

    public String getName() {
        return name;
    }

    public String getPlatform() {
        return platform;
    }

    public String getGenre() {
        return genre;
    }

    public String getPublisher() {
        return publisher;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Game game = (Game) o;
        return name.equals(game.name) &&
                platform.equals(game.platform) &&
                version.equals(game.version);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(name, platform, version);
    }

    @Override
    public String toString() {
        return name + " (" + platform + ")";
    }
}
