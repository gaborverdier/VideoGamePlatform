package org.example.models;
import com.gaming.api.models.GameModel;
import javafx.scene.image.Image;
import java.util.ArrayList;
import java.util.List;

public class Game {
    private String id;
    private String name;
    private double price;
    private String genre;
    private String publisherId;
    private String publisherName;
    private String coverImageUrl;
    private Image coverImage;
    private boolean owned;
    private String description;
    private double rating;
    private int playtime;
    private String platform;
    
    // Nouveaux attributs
    private boolean isInstalled = false;
    private boolean isFavorite = false;
    private boolean isWishlisted = false;
    private int playedTime = 0; // en minutes
    private List<String> availableUpdates = new ArrayList<>();
    private List<String> installedUpdates = new ArrayList<>();
    private List<DLC> availableDLCs = new ArrayList<>();
    private List<Review> reviews = new ArrayList<>();
    // Version info
    private String version; // backend/latest version
    private String installedVersion; // locally installed version
    // Cached total played time reported by platform (milliseconds) - may be null if unknown
    private Long totalPlayedAllTimeMs = null;
    
    // Constructeur complet
    public Game(String id, String name, double price, String genre, String publisherId, String publisherName,
                String coverImageUrl, String description, double rating, int playtime,
                String platform) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.genre = genre;
        this.publisherId = publisherId;
        this.publisherName = publisherName;
        this.coverImageUrl = coverImageUrl;
        this.coverImage = new Image(coverImageUrl, true);
        this.owned = false;
        this.description = description;
        this.rating = rating;
        this.playtime = playtime;
        this.platform = platform != null ? platform : "PC";
    }
    
    // Constructeur complet avec plateforme par défaut (PC)
    public Game(String id, String name, double price, String genre, String publisherId, String publisherName,
                String coverImageUrl, String description, double rating, int playtime) {
        this(id, name, price, genre, publisherId, publisherName, coverImageUrl, description, rating, playtime, "PC");
    }

    // Constructeur simplifié
    public Game(String name, double price, String genre, String coverImageUrl,
                String description, double rating, int playtime, String platform) {
        this(generateId(), name, price, genre, "PUB-" + System.currentTimeMillis(),
             "Unknown Publisher", coverImageUrl, description, rating, playtime, platform);
    }

    // Constructeur simplifié avec plateforme par défaut (PC)
    public Game(String name, double price, String genre, String coverImageUrl,
                String description, double rating, int playtime) {
        this(name, price, genre, coverImageUrl, description, rating, playtime, "PC");
    }
    
    
    private static String generateId() {
        return "GAME-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
    }
    
    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getGenre() { return genre; }
    public String getPublisherId() { return publisherId; }
    public String getPublisherName() { return publisherName; }
    public String getCoverImageUrl() { return coverImageUrl; }
    public Image getCoverImage() { return coverImage; }
    public boolean isOwned() { return owned; }
    public String getDescription() { return description; }
    public double getRating() { return rating; }
    public int getPlaytime() { return playtime; }
    public String getPlatform() { return platform; }
    public boolean isInstalled() { return isInstalled; }
    public boolean isFavorite() { return isFavorite; }
    public boolean isWishlisted() { return isWishlisted; }
    public int getPlayedTime() { return playedTime; }
    public List<String> getAvailableUpdates() { return availableUpdates; }
    public List<String> getInstalledUpdates() { return installedUpdates; }
    public List<DLC> getAvailableDLCs() { return availableDLCs; }
    public List<Review> getReviews() { return reviews; }
    
    // Setters
    public void setOwned(boolean owned) { this.owned = owned; }
    public void setInstalled(boolean installed) { this.isInstalled = installed; }
    public void setFavorite(boolean favorite) { this.isFavorite = favorite; }
    public void setWishlisted(boolean wishlisted) { this.isWishlisted = wishlisted; }
    public void setPrice(double price) { this.price = price; }
    public void setPlatform(String platform) { this.platform = platform; }
    
    // Méthodes
    public void purchase() {
        this.owned = true;
        this.isWishlisted = false;
    }    public void addPlayedTime(int minutes) {
        if (this.playedTime + minutes >= 0) {
            this.playedTime += minutes;
        }
    }
    
    public void addUpdate(String update) {
        if (!availableUpdates.contains(update)) {
            availableUpdates.add(update);
        }
    }
    
    public void installUpdate(String update) {
        if (availableUpdates.contains(update) && !installedUpdates.contains(update)) {
            installedUpdates.add(update);
        }
    }
    
    public void addDLC(String name, double price) {
        boolean exists = availableDLCs.stream().anyMatch(d -> d.getName().equals(name));
        if (!exists) {
            availableDLCs.add(new DLC(name, price));
        }
    }

    // add DLC with explicit id (from backend)
    public void addDLC(String id, String name, double price) {
        boolean exists = availableDLCs.stream().anyMatch(d -> d.getId() != null && d.getId().equals(id));
        if (!exists) {
            availableDLCs.add(new DLC(id, name, price));
        }
    }

    public void installDLC(DLC dlc) {
        dlc.setInstalled(true);
    }

    public List<DLC> getPendingDLCs() {
        return availableDLCs.stream()
            .filter(dlc -> ! dlc.isInstalled())
            .collect(java.util.stream.Collectors. toList());
    }
    
    public void addReview(Review review) {
        reviews.add(review);
    }
    
    public double getAverageRating() {
        if (reviews.isEmpty()) return rating;
        return reviews.stream().mapToDouble(Review::getRating).average().orElse(rating);
    }
    
    public List<String> getPendingUpdates() {
        List<String> pending = new ArrayList<>(availableUpdates);
        pending.removeAll(installedUpdates);
        return pending;
    }
    
    public String getFormattedPrice() {
        if (price == 0.0) {
            return "GRATUIT";
        }
        return String.format("%.2f€", price);
    }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getInstalledVersion() { return installedVersion; }
    public void setInstalledVersion(String installedVersion) { this.installedVersion = installedVersion; }

    // Backend-reported totals (cached)
    public Long getTotalPlayedAllTimeMs() { return totalPlayedAllTimeMs; }
    public void setTotalPlayedAllTimeMs(Long totalPlayedAllTimeMs) { this.totalPlayedAllTimeMs = totalPlayedAllTimeMs; }
    public Long getTotalPlayedAllTimeMinutes() { return totalPlayedAllTimeMs == null ? null : (totalPlayedAllTimeMs / 60_000L); }
    
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

    // Classe interne pour les DLCs
    public static class DLC {
        private String id;
        private String name;
        private double price;
        private boolean installed;
        private int playedTime = 0;
        private List<Review> reviews = new ArrayList<>();
        
        public DLC(String name, double price) {
            this("DLC-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000), name, price);
        }

        public DLC(String id, String name, double price) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.installed = false;
        }
        
        public String getId() { return id; }
        public String getName() { return name; }
        public double getPrice() { return price; }
        public boolean isInstalled() { return installed; }
        public void setInstalled(boolean installed) { this.installed = installed; }
        public int getPlayedTime() { return playedTime; }
        public void addPlayedTime(int minutes) { this.playedTime += minutes; }
        public List<Review> getReviews() { return reviews; }
        public void addReview(Review review) { reviews.add(review); }
        
        public double getAverageRating() {
            if (reviews.isEmpty()) return 0;
            return reviews.stream().mapToDouble(Review::getRating).average().orElse(0);
        }
        
        public String getFormattedPrice() {
            return String.format("%.2f€", price);
        }
    }

    // Mapping: Avro GameModel -> Local Game
    public static Game fromAvroModel(GameModel avro) {
        return new Game(
            avro.getGameId(),
            avro.getTitle(),
            avro.getPrice(),
            avro.getGenre(),
            null, // publisherId not present in Avro
            avro.getPublisherName(),
            "https://www.boredpanda.com/blog/wp-content/uploads/2025/10/funny-cat-memes-go-hard-cover_675.jpg",
            avro.getDescription(),
            0.0, // rating not present in Avro
            0, // playtime not present in Avro
            avro.getPlatform() // platform from Avro
        );
    }

    // Try to extract version from Avro model if present
    public static Game fromAvroModelWithVersion(GameModel avro) {
        Game g = fromAvroModel(avro);
        try {
            java.lang.reflect.Method m = avro.getClass().getMethod("getVersion");
            if (m != null) {
                Object v = m.invoke(avro);
                if (v != null) g.setVersion(String.valueOf(v));
            }
        } catch (Exception ignore) {}
        return g;
    }
}