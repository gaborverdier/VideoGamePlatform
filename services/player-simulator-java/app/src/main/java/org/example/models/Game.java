package org.example. models;
import com.gaming.api.models.GameModel;
import javafx.scene.image.Image;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    private Set<Platform> supportedPlatforms;
    private Set<Platform> ownedPlatforms;
    
    // Nouveaux attributs
    private boolean isInstalled = false;
    private boolean isFavorite = false;
    private boolean isWishlisted = false;
    private int playedTime = 0; // en minutes
    private List<String> availableUpdates = new ArrayList<>();
    private List<String> installedUpdates = new ArrayList<>();
    private List<DLC> availableDLCs = new ArrayList<>();
    private List<Review> reviews = new ArrayList<>();
    
    // Constructeur complet
    public Game(String id, String name, double price, String genre, String publisherId, String publisherName,
                String coverImageUrl, String description, double rating, int playtime,
                Set<Platform> supportedPlatforms) {
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
        this.supportedPlatforms = normalizeSupportedPlatforms(supportedPlatforms);
        this.ownedPlatforms = EnumSet.noneOf(Platform.class);
    }
    
    // Constructeur complet avec plateformes par défaut (PC)
    public Game(String id, String name, double price, String genre, String publisherId, String publisherName,
                String coverImageUrl, String description, double rating, int playtime) {
        this(id, name, price, genre, publisherId, publisherName, coverImageUrl, description, rating, playtime,
                EnumSet.of(Platform.PC));
    }

    // Constructeur simplifié
    public Game(String name, double price, String genre, String coverImageUrl,
                String description, double rating, int playtime, Set<Platform> supportedPlatforms) {
        this(generateId(), name, price, genre, "PUB-" + System.currentTimeMillis(),
             "Unknown Publisher", coverImageUrl, description, rating, playtime, supportedPlatforms);
    }

    // Constructeur simplifié avec plateformes par défaut (PC)
    public Game(String name, double price, String genre, String coverImageUrl,
                String description, double rating, int playtime) {
        this(name, price, genre, coverImageUrl, description, rating, playtime, EnumSet.of(Platform.PC));
    }
    
    private static String generateId() {
        return "GAME-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
    }

    private Set<Platform> normalizeSupportedPlatforms(Set<Platform> platforms) {
        if (platforms == null || platforms.isEmpty()) {
            return EnumSet.of(Platform.PC);
        }
        return EnumSet.copyOf(platforms);
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
    public Set<Platform> getSupportedPlatforms() { return EnumSet.copyOf(supportedPlatforms); }
    public Set<Platform> getOwnedPlatforms() { return EnumSet.copyOf(ownedPlatforms); }
    public boolean isInstalled() { return isInstalled; }
    public boolean isFavorite() { return isFavorite; }
    public boolean isWishlisted() { return isWishlisted; }
    public int getPlayedTime() { return playedTime; }
    public List<String> getAvailableUpdates() { return availableUpdates; }
    public List<String> getInstalledUpdates() { return installedUpdates; }
    public List<DLC> getAvailableDLCs() { return availableDLCs; }
    public List<Review> getReviews() { return reviews; }
    public boolean isOwnedOnPlatform(Platform platform) { return platform != null && ownedPlatforms.contains(platform); }
    public boolean ownsAllSupportedPlatforms() { return ownedPlatforms.containsAll(supportedPlatforms); }
    public String getSupportedPlatformsLabel() { return supportedPlatforms.stream().map(Platform::getLabel).collect(Collectors.joining(", ")); }
    public String getOwnedPlatformsLabel() { return ownedPlatforms.stream().map(Platform::getLabel).collect(Collectors.joining(", ")); }
    
    // Setters
    public void setOwned(boolean owned) { this.owned = owned; }
    public void setInstalled(boolean installed) { this.isInstalled = installed; }
    public void setFavorite(boolean favorite) { this.isFavorite = favorite; }
    public void setWishlisted(boolean wishlisted) { this.isWishlisted = wishlisted; }
    public void setPrice(double price) { this.price = price; }
    
    // Méthodes
    public boolean purchase(Platform platform) {
        if (platform == null || !supportedPlatforms.contains(platform)) {
            return false;
        }
        ownedPlatforms.add(platform);
        this.owned = true;
        this.isWishlisted = false;
        return true;
    }
    
    public void addPlayedTime(int minutes) {
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
        availableDLCs.add(new DLC(name, price));
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
            this.id = "DLC-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
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
    
    // Mapping: Local Game -> Avro GameModel
    public GameModel toAvroModel() {
        GameModel avro = new GameModel();
        avro.setGameId(this.id);
        avro.setTitle(this.name);
        avro.setPublisher(this.publisherName);
        avro.setPlatform(this.supportedPlatforms != null && !this.supportedPlatforms.isEmpty() ? this.supportedPlatforms.iterator().next().name() : null);
        avro.setGenre(this.genre);
        avro.setReleaseYear(null); // Not present in local model
        avro.setPrice(this.price);
        avro.setVersion("1.0"); // Or use a real version if available
        avro.setAvailable(true); // Or use a real flag if available
        avro.setDescription(this.description);
        return avro;
    }

    // Mapping: Avro GameModel -> Local Game
    public static Game fromAvroModel(GameModel avro) {
        return new Game(
            avro.getGameId(),
            avro.getTitle(),
            avro.getPrice(),
            avro.getGenre(),
            null, // publisherId not present in Avro
            avro.getPublisher(),
            "https://www.boredpanda.com/blog/wp-content/uploads/2025/10/funny-cat-memes-go-hard-cover_675.jpg",
            avro.getDescription(),
            0.0, // rating not present in Avro
            0, // playtime not present in Avro
            null // supportedPlatforms not present in Avro
        );
    }
}