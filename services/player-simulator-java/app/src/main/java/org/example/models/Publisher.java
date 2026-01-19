package org.example.models;

import java.util.ArrayList;
import java.util.List;

public class Publisher {
    private String id;
    private String name;
    private String description;
    private String logoUrl;
    private List<String> publishedGameIds;
    
    public Publisher(String id, String name, String description, String logoUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.logoUrl = logoUrl;
        this.publishedGameIds = new ArrayList<>();
    }
    
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getLogoUrl() { return logoUrl; }
    public List<String> getPublishedGameIds() { return new ArrayList<>(publishedGameIds); }
    
    public void addGame(String gameId) {
        if (!publishedGameIds.contains(gameId)) {
            publishedGameIds.add(gameId);
        }
    }
}