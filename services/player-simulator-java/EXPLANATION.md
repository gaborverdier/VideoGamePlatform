# 🎮 GUIDE D'EXPLICATION DÉTAILLÉ - Player Simulator

Ce document explique en détail **comment fonctionne** le Player Simulator, l'application JavaFX qui simule le comportement d'un joueur.

---

## 📚 Table des Matières

1. [Vue d'ensemble de l'architecture](#1-vue-densemble-de-larchitecture)
2. [Architecture JavaFX (MVC)](#2-architecture-javafx-mvc)
3. [Flux utilisateur détaillés](#3-flux-utilisateur-détaillés)
4. [Intégration API REST](#4-intégration-api-rest)
5. [Production d'événements Kafka](#5-production-dévénements-kafka)
6. [Composants UI expliqués](#6-composants-ui-expliqués)

---

## 1. Vue d'ensemble de l'architecture

### 1.1 Architecture globale

```
┌─────────────────────────────────────────────────────────────┐
│              Player Simulator (JavaFX Application)           │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌──────────────────┐          ┌──────────────────┐        │
│  │   Views (UI)     │<────────>│   Controllers    │        │
│  │  - LoginDialog   │          │  - Library       │        │
│  │  - Dashboard     │          │  - Dashboard     │        │
│  │  - Tabs          │          │                  │        │
│  └──────────────────┘          └──────────────────┘        │
│         │                               │                   │
│         │                               ▼                   │
│         │                      ┌──────────────────┐        │
│         │                      │    Services      │        │
│         │                      │  - GameData      │        │
│         │                      │  - Session       │        │
│         │                      │  - Notification  │        │
│         │                      └──────────────────┘        │
│         │                               │                   │
│         ▼                               ▼                   │
│  ┌──────────────────┐          ┌──────────────────┐        │
│  │  Kafka Producer  │          │  REST API Client │        │
│  │  (Events)        │          │  (HTTP)          │        │
│  └──────────────────┘          └──────────────────┘        │
└─────────────────────────────────────────────────────────────┘
         │                               │
         │                               │
         ▼                               ▼
   Apache Kafka                  Platform Service
   (Topics)                      (REST API)
```

---

### 1.2 Pattern MVC (Model-View-Controller)

**Model** = Objets métier
```java
public class Game {
    private String id;
    private String title;
    private String genre;
    private Double price;
    private Integer playedTime;
    // ...
}

public class Player {
    private String id;
    private String username;
    private String email;
    private Double wallet;
    private List<Game> ownedGames;
    // ...
}
```

**View** = Composants JavaFX
```java
public class LibraryTab extends VBox {
    private FlowPane gamesGrid;
    private TextField searchField;
    // Construction de l'interface
}
```

**Controller** = Logique de l'application
```java
public class LibraryController {
    private PlatformApiClient platformApi;
    
    public List<Game> loadAllGames() {
        // Appel API + mapping
    }
}
```

---

## 2. Architecture JavaFX (MVC)

### 2.1 Cycle de vie d'une Application JavaFX

```
1. main() 
   ↓
2. Application.launch(args)
   ↓
3. JavaFX Toolkit démarre
   ↓
4. start(Stage primaryStage) appelé
   ↓
5. Construction de la scène (Scene)
   ↓
6. Affichage de la fenêtre (Stage.show())
   ↓
7. Boucle d'événements (Event Loop)
   ↓
8. stop() appelé lors de la fermeture
```

**Code:**
```java
public class PlayerDashboard extends Application {
    
    @Override
    public void start(Stage stage) {
        // 1. Afficher login
        if (!LoginDialog.show()) {
            return; // Utilisateur quitte
        }
        
        // 2. Initialiser services
        KafkaProducerService kafkaProducer = new KafkaProducerService(...);
        PlayerDashboardController controller = new PlayerDashboardController(...);
        
        // 3. Créer les tabs
        LibraryTab libraryTab = new LibraryTab(...);
        MyGamesTab myGamesTab = new MyGamesTab(...);
        // ...
        
        // 4. Construire la scène
        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(libraryTab, myGamesTab, ...);
        
        Scene scene = new Scene(tabPane, 1200, 800);
        
        // 5. Afficher
        stage.setTitle("Player Dashboard");
        stage.setScene(scene);
        stage.show();
    }
    
    public static void main(String[] args) {
        launch(args); // Lance le cycle de vie JavaFX
    }
}
```

---

### 2.2 Structure des composants

```
PlayerDashboard (Application)
  │
  ├─> LoginDialog (Modal)
  │     ├─> Login form
  │     └─> Register form
  │
  └─> TabPane (Container)
        ├─> LibraryTab (Browse games)
        │     ├─> Search bar
        │     ├─> Games grid
        │     └─> GameDetailsDialog
        │
        ├─> MyGamesTab (Owned games)
        │     ├─> Games list
        │     └─> GamePlayDialog
        │           ├─> Play button
        │           ├─> Time tracker
        │           └─> Crash button
        │
        ├─> WishlistTab (Wishlist)
        ├─> NotificationsTab (Notifications)
        ├─> PublishersTab (Followed publishers)
        └─> FriendsTab (Friends list)
```

---

## 3. Flux utilisateur détaillés

### 3.1 Flux : Connexion

```
1. Application démarre
   ↓
2. LoginDialog.show()
   ┌─────────────────────────────┐
   │  Username: [__________]     │
   │  Email:    [__________]     │
   │  [Login]    [Register]      │
   └─────────────────────────────┘
   ↓
3. Utilisateur clique "Login"
   ↓
4. Validation (username non vide, email valide)
   ↓
5. GET /api/users/username/{username}
   ↓ HTTP Response
6. Si trouvé → SessionManager.login(user)
   Si non trouvé → Afficher erreur
   ↓
7. Dialog se ferme, Dashboard s'affiche
```

**Code:**
```java
public class LoginDialog {
    
    public static boolean show() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        
        TextField usernameField = new TextField();
        TextField emailField = new TextField();
        
        Button loginBtn = new Button("Login");
        loginBtn.setOnAction(e -> {
            String username = usernameField.getText();
            
            try {
                // Appel API
                PlatformApiClient api = new PlatformApiClient();
                String json = api.getUserByUsernameJson(username);
                
                // Parsing JSON
                ObjectMapper mapper = new ObjectMapper();
                UserModel user = mapper.readValue(json, UserModel.class);
                
                // Sauvegarde session
                Player player = Player.fromUserModel(user);
                SessionManager.getInstance().login(player);
                
                dialog.close();
            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Utilisateur non trouvé");
                alert.show();
            }
        });
        
        // Construction UI...
        dialog.showAndWait();
        
        return SessionManager.getInstance().isLoggedIn();
    }
}
```

---

### 3.2 Flux : Achat d'un jeu

```
1. LibraryTab affiche tous les jeux disponibles
   ↓
2. Utilisateur clique sur un jeu
   ↓
3. GameDetailsDialog s'ouvre
   ┌────────────────────────────────┐
   │  🎮 The Legend of Zelda        │
   │  Genre: Action/Adventure       │
   │  Price: $59.99                 │
   │  ⭐⭐⭐⭐⭐ (4.8/5)              │
   │                                │
   │  [Acheter]  [Wishlist]         │
   └────────────────────────────────┘
   ↓
4. Utilisateur clique "Acheter"
   ↓
5. Validation (suffisamment d'argent ?)
   ↓
6. POST /api/purchases
   Body: {"userId": "...", "gameId": "...", "price": 59.99}
   ↓ HTTP 201 CREATED
7. Mise à jour du wallet
   Player.wallet -= 59.99
   ↓
8. Mise à jour de l'UI
   - LibraryTab: refresh
   - MyGamesTab: affiche le nouveau jeu
   - Wallet label: mise à jour
   ↓
9. Alert success
   "Jeu acheté avec succès !"
```

**Code:**
```java
Button buyBtn = new Button("Acheter " + game.getFormattedPrice());
buyBtn.setOnAction(e -> {
    Player player = SessionManager.getInstance().getCurrentPlayer();
    
    // Validation
    if (player.getWallet() < game.getPrice()) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText("Fonds insuffisants");
        alert.show();
        return;
    }
    
    try {
        // Appel API
        PlatformApiClient api = new PlatformApiClient();
        String requestBody = String.format(
            "{\"userId\":\"%s\",\"gameId\":\"%s\",\"price\":%.2f}",
            player.getId(), game.getId(), game.getPrice()
        );
        String response = api.postPurchaseJson(requestBody);
        
        // Mise à jour locale
        player.setWallet(player.getWallet() - game.getPrice());
        player.addOwnedGame(game);
        
        // Refresh UI
        onGamePurchased.run();
        
        // Success alert
        Alert success = new Alert(Alert.AlertType.INFORMATION);
        success.setContentText("Jeu acheté avec succès !");
        success.show();
        
    } catch (Exception ex) {
        Alert error = new Alert(Alert.AlertType.ERROR);
        error.setContentText("Erreur lors de l'achat");
        error.show();
    }
});
```

---

### 3.3 Flux : Jouer à un jeu

```
1. MyGamesTab affiche les jeux possédés
   ↓
2. Utilisateur clique "Jouer"
   ↓
3. GamePlayDialog s'ouvre
   ┌────────────────────────────────┐
   │  🎮 En jeu : Zelda             │
   │  Temps de jeu: 45 min          │
   │                                │
   │  [+10 min]  [-10 min]          │
   │                                │
   │  Crash type: [99 - Unknown ▼] │
   │  Details:    [____________]    │
   │  [CRASH LE JEU]                │
   │                                │
   │  [Arrêter de jouer]            │
   └────────────────────────────────┘
   ↓
4. Utilisateur clique "+10 min" 5 fois
   → playedTime += 50 minutes
   ↓
5. Utilisateur clique "Arrêter de jouer"
   ↓
6. Sauvegarde de la session
   POST /api/session
   Body: {
     "userId": "...",
     "gameId": "...",
     "startTimestamp": 1738000000,
     "timePlayed": 3000000  // 50 min en ms
   }
   ↓
7. Production event Kafka
   Topic: game-session-ended
   Event: GameSessionEnded(
     sessionId: "...",
     gameId: "...",
     duration: 3000000
   )
   ↓
8. Dialog se ferme
   MyGamesTab: refresh (temps de jeu mis à jour)
```

---

### 3.4 Flux : Crash d'un jeu

```
1. GamePlayDialog ouvert
   ↓
2. Utilisateur sélectionne crash type "1 - Graphics"
   ↓
3. Utilisateur entre "Texture flickering" dans details
   ↓
4. Utilisateur clique "CRASH LE JEU"
   ↓
5. PlayerDashboardController.reportCrash()
   ↓
6. Production event Kafka
   Topic: game-crash-reported
   Event: GameCrashReported(
     crashId: "crash-abc123",
     gameId: "game-456",
     userId: "user-123",
     crashCode: 1,
     crashMessage: "Texture flickering",
     crashTimestamp: 1738000000,
     gameVersion: "1.0"
   )
   ↓
7. Alert error s'affiche
   ┌────────────────────────────────┐
   │  💥 CRASH !                    │
   │  Le jeu a planté               │
   │  Code: 1 - Graphics            │
   │  [OK]                          │
   └────────────────────────────────┘
   ↓
8. Dialog se ferme
```

**Code:**
```java
public void reportCrash(String gameId, String gameVersion, int crashCode, String message) {
    String crashId = UUID.randomUUID().toString();
    
    GameCrashReported event = GameCrashReported.newBuilder()
        .setCrashId(crashId)
        .setGameId(gameId)
        .setUserId(userId)
        .setCrashCode(crashCode)
        .setCrashMessage(message)
        .setCrashTimestamp(System.currentTimeMillis())
        .setGameVersion(gameVersion)
        .build();
    
    kafkaProducer.sendGameCrashReported(event);
    
    System.out.println("💥 Crash reported: " + crashId);
}
```

---

## 4. Intégration API REST

### 4.1 PlatformApiClient (HTTP Client)

```java
public class PlatformApiClient {
    private static final String BASE_URL = "http://localhost:8082/api";
    
    // GET /api/games
    public String getAllGamesJson() throws Exception {
        return ApiClient.get("/api/games");
    }
    
    // GET /api/games/{gameId}
    public String getGameByIdJson(String gameId) throws Exception {
        return ApiClient.get("/api/games/" + gameId);
    }
    
    // POST /api/purchases
    public String postPurchaseJson(String jsonBody) throws Exception {
        return ApiClient.post("/api/purchases", jsonBody);
    }
    
    // GET /api/library/user/{userId}
    public String getUserLibraryJson(String userId) throws Exception {
        return ApiClient.get("/api/library/user/" + userId);
    }
}
```

**ApiClient interne:**
```java
public class ApiClient {
    
    public static String get(String endpoint) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:8082" + endpoint))
            .header("Content-Type", "application/json")
            .GET()
            .build();
        
        HttpResponse<String> response = client.send(request, 
            HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new RuntimeException("HTTP " + response.statusCode());
        }
        
        return response.body();
    }
    
    public static String post(String endpoint, String body) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:8082" + endpoint))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
        
        HttpResponse<String> response = client.send(request,
            HttpResponse.BodyHandlers.ofString());
        
        return response.body();
    }
}
```

---

### 4.2 Mapping JSON → Objets Java

```java
public List<Game> loadAllGames() {
    try {
        // 1. Appel API (retourne JSON)
        String json = platformApi.getAllGamesJson();
        // json = "[{\"id\":\"game-123\",\"title\":\"Zelda\",...},...]"
        
        // 2. Parsing JSON → List<GameModel>
        ObjectMapper mapper = new ObjectMapper();
        List<GameModel> avroGames = mapper.readValue(json, 
            new TypeReference<List<GameModel>>() {});
        
        // 3. Conversion GameModel → Game
        return avroGames.stream()
            .map(Game::fromAvroModelWithVersion)
            .collect(Collectors.toList());
        
    } catch (Exception e) {
        throw new RuntimeException("Failed to load games", e);
    }
}
```

**Conversion Avro → Domain:**
```java
public static Game fromAvroModelWithVersion(GameModel avro) {
    Game game = new Game();
    game.setId(avro.getId());
    game.setTitle(avro.getTitle());
    game.setGenre(avro.getGenre());
    game.setPrice(avro.getPrice());
    game.setPlatform(avro.getPlatform());
    game.setDescription(avro.getDescription());
    game.setVersion(avro.getVersion()); // Nouveau champ
    return game;
}
```

---

## 5. Production d'événements Kafka

### 5.1 KafkaProducerService

```java
public class KafkaProducerService {
    private final KafkaProducer<String, SpecificRecordBase> producer;
    
    public KafkaProducerService(String bootstrapServers, String schemaRegistryUrl) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        props.put("schema.registry.url", schemaRegistryUrl);
        
        this.producer = new KafkaProducer<>(props);
    }
    
    public void sendGameCrashReported(GameCrashReported event) {
        ProducerRecord<String, SpecificRecordBase> record = 
            new ProducerRecord<>(
                "game-crash-reported",  // Topic
                event.getGameId(),      // Clé (partition par gameId)
                event                   // Valeur (event Avro)
            );
        
        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                System.err.println("❌ Erreur Kafka: " + exception.getMessage());
            } else {
                System.out.println("✅ Event envoyé - Partition: " + 
                    metadata.partition() + ", Offset: " + metadata.offset());
            }
        });
    }
}
```

---

### 5.2 Événements produits

**GameSessionStarted:**
```java
GameSessionStarted event = GameSessionStarted.newBuilder()
    .setSessionId(UUID.randomUUID().toString())
    .setGameId(gameId)
    .setGameTitle(gameTitle)
    .setGameVersion(gameVersion)
    .setUserId(userId)
    .setUsername(username)
    .setStartTimestamp(System.currentTimeMillis())
    .setPlatform(platform)
    .build();

kafkaProducer.sendGameSessionStarted(event);
```

**GameSessionEnded:**
```java
GameSessionEnded event = GameSessionEnded.newBuilder()
    .setSessionId(sessionId)
    .setGameId(gameId)
    .setUserId(userId)
    .setEndTimestamp(System.currentTimeMillis())
    .setDuration(durationMs)
    .build();

kafkaProducer.sendGameSessionEnded(event);
```

**GameReviewed:**
```java
GameReviewed event = GameReviewed.newBuilder()
    .setReviewId(UUID.randomUUID().toString())
    .setGameId(gameId)
    .setUserId(userId)
    .setRating(rating)  // 1-5
    .setComment(comment)
    .setPlaytimeMinutes(playtime)
    .setReviewTimestamp(System.currentTimeMillis())
    .build();

kafkaProducer.sendGameReviewed(event);
```

---

## 6. Composants UI expliqués

### 6.1 LibraryTab (Browse Games)

**Structure:**
```
LibraryTab (VBox)
  │
  ├─> HBox (Search bar)
  │     ├─> TextField searchField
  │     └─> Button searchBtn
  │
  └─> ScrollPane
        └─> FlowPane gamesGrid
              ├─> VBox (Game card 1)
              │     ├─> ImageView (cover)
              │     ├─> Label (title)
              │     ├─> Label (price)
              │     └─> Button (details)
              │
              ├─> VBox (Game card 2)
              └─> ...
```

**Code:**
```java
public class LibraryTab extends VBox {
    
    public LibraryTab(Consumer<Game> onGamePurchased) {
        // Search bar
        TextField searchField = new TextField();
        searchField.setPromptText("Rechercher un jeu...");
        
        // Games grid
        FlowPane gamesGrid = new FlowPane();
        gamesGrid.setHgap(15);
        gamesGrid.setVgap(15);
        
        // Load games
        LibraryController controller = new LibraryController(new PlatformApiClient());
        List<Game> allGames = controller.loadAllGames();
        
        // Create game cards
        for (Game game : allGames) {
            VBox card = createGameCard(game, onGamePurchased);
            gamesGrid.getChildren().add(card);
        }
        
        // Layout
        this.getChildren().addAll(searchField, new ScrollPane(gamesGrid));
    }
    
    private VBox createGameCard(Game game, Consumer<Game> onPurchase) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #2b2b2b; -fx-padding: 15px;");
        
        ImageView cover = new ImageView(game.getCoverImage());
        cover.setFitWidth(150);
        cover.setFitHeight(200);
        
        Label title = new Label(game.getTitle());
        title.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        
        Label price = new Label(game.getFormattedPrice());
        
        Button detailsBtn = new Button("Voir détails");
        detailsBtn.setOnAction(e -> {
            GameDetailsDialog.show(game, onPurchase);
        });
        
        card.getChildren().addAll(cover, title, price, detailsBtn);
        return card;
    }
}
```

---

### 6.2 MyGamesTab (Owned Games)

**Fonctionnalités:**
- Affiche les jeux possédés
- Temps de jeu affiché
- Bouton "Jouer" ouvre GamePlayDialog
- Affiche la version du jeu

```java
private VBox createGameCard(Game game, Runnable onRefresh) {
    VBox card = new VBox(10);
    
    Label title = new Label(game.getTitle());
    Label playtime = new Label("Temps de jeu: " + game.getPlayedTime() + " min");
    Label version = new Label("Version: " + game.getVersion());
    
    Button playBtn = new Button("▶ Jouer");
    playBtn.setOnAction(e -> {
        PlayerDashboardController controller = 
            SessionManager.getInstance().getPlayerController();
        GamePlayDialog.show(game, onRefresh, controller);
    });
    
    card.getChildren().addAll(title, playtime, version, playBtn);
    return card;
}
```

---

### 6.3 NotificationsTab (Notifications)

**Types de notifications:**
1. **Nouveau patch disponible** : "Le jeu 'Zelda' a une nouvelle version 1.2.0 !"
2. **Nouveau DLC disponible** : "DLC 'The Master Trials' disponible pour Zelda"
3. **Ami a acheté un jeu** : "Votre ami JohnDoe a acheté Zelda"

```java
public class NotificationsTab extends ScrollPane {
    
    public NotificationsTab() {
        VBox notificationsList = new VBox(10);
        notificationsList.setPadding(new Insets(20));
        
        // Load notifications
        NotificationService service = new NotificationService();
        List<Notification> notifs = service.getUserNotifications(
            SessionManager.getInstance().getCurrentPlayer().getId()
        );
        
        // Sort by timestamp (most recent first)
        notifs.sort(Comparator.comparing(Notification::getTimestamp).reversed());
        
        // Create notification cards
        for (Notification notif : notifs) {
            HBox card = createNotificationCard(notif);
            notificationsList.getChildren().add(card);
        }
        
        this.setContent(notificationsList);
    }
    
    private HBox createNotificationCard(Notification notif) {
        HBox card = new HBox(15);
        card.setStyle("-fx-background-color: #2b2b2b; -fx-padding: 15px;");
        
        Label icon = new Label(getIconForType(notif.getType()));
        Label description = new Label(notif.getDescription());
        Label time = new Label(formatTimestamp(notif.getTimestamp()));
        
        card.getChildren().addAll(icon, description, time);
        return card;
    }
}
```

---

## 🎓 Conclusion

Le Player Simulator est une **application JavaFX complète** qui :

✅ **Interface riche** - 6 tabs (Library, MyGames, Wishlist, Notifications, Publishers, Friends)  
✅ **Intégration API REST** - Communication HTTP avec Platform Service  
✅ **Production Kafka** - Événements sessions, crashs, reviews  
✅ **MVC pattern** - Séparation Views / Controllers / Models  
✅ **Reactive UI** - Mise à jour automatique après achats/sessions  
✅ **Session management** - Login/logout avec SessionManager  
✅ **Error handling** - Alerts pour erreurs réseau/métier  

**Événements Kafka produits:**
- `game-session-started` - Début d'une session de jeu
- `game-session-ended` - Fin d'une session
- `game-crash-reported` - Crash d'un jeu
- `game-reviewed` - Avis/note sur un jeu

**API REST consommées:**
- GET `/api/games` - Catalogue de jeux
- GET `/api/library/user/{id}` - Bibliothèque utilisateur
- POST `/api/purchases` - Achat d'un jeu
- POST `/api/wishlist/new` - Ajout à la wishlist
- GET `/api/notifications/user/{id}` - Notifications
