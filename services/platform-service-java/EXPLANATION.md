# 🎮 GUIDE D'EXPLICATION DÉTAILLÉ - Platform Service

Ce document explique en détail **comment fonctionne** chaque partie du Platform Service, le service central de la plateforme de jeux vidéo.

---

## 📚 Table des Matières

1. [Vue d'ensemble de l'architecture](#1-vue-densemble-de-larchitecture)
2. [Flux de données détaillés](#2-flux-de-données-détaillés)
3. [Explication des patterns utilisés](#3-explication-des-patterns-utilisés)
4. [Composants clés expliqués](#4-composants-clés-expliqués)
5. [Gestion des erreurs et résilience](#5-gestion-des-erreurs-et-résilience)
6. [Performance et optimisations](#6-performance-et-optimisations)

---

## 1. Vue d'ensemble de l'architecture

### 1.1 Architecture en couches (Layered Architecture)

```
┌─────────────────────────────────────────────────────────────┐
│                   PRESENTATION LAYER                         │
│  - REST Controllers (GameController, UserController, etc.)  │
│  - Validation des entrées                                   │
│  - Sérialisation JSON                                       │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ Model Objects
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                   SERVICE LAYER                              │
│  - GameService (gestion catalogue jeux)                     │
│  - UserService (gestion utilisateurs)                       │
│  - PurchaseService (achat de jeux)                          │
│  - LibraryService (bibliothèque utilisateur)                │
│  - SessionService (sessions de jeu)                         │
│  - WishlistService (liste de souhaits)                      │
│  - ReviewService (avis/notes)                               │
│  - DLCService (contenu téléchargeable)                      │
│  - NotificationsService (notifications utilisateur)         │
│  - Transactions (@Transactional)                            │
│  - Règles métier                                             │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ JPA Entities
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                   REPOSITORY LAYER                           │
│  - UserRepository                                           │
│  - GameRepository                                           │
│  - PurchaseRepository                                       │
│  - SessionRepository                                        │
│  - ReviewRepository                                         │
│  - WishlistRepository                                       │
│  - NotificationRepository                                   │
│  - Spring Data JPA (génère SQL automatiquement)            │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ JDBC
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                   PERSISTENCE LAYER                          │
│  - H2 Database (dev) / PostgreSQL (prod)                   │
│  - Tables: users, games, purchases, sessions, etc.         │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                   MESSAGING LAYER (Consumers)                │
│  - GameReleasedConsumer                                     │
│  - PatchReleasedConsumer                                    │
│  - DLCPurchasedConsumer                                     │
│  - Désérialisation Avro                                     │
│  - Threads séparés (@KafkaListener)                         │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ Kafka Protocol
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                   APACHE KAFKA                               │
│  - Topics: game-released, game-patch-released, dlc-created │
│  - Schema Registry (validation Avro)                        │
└─────────────────────────────────────────────────────────────┘
```

**Pourquoi cette architecture ?**

1. **Séparation des préoccupations** : Chaque couche a une responsabilité unique
2. **Testabilité** : On peut tester chaque couche indépendamment
3. **Maintenabilité** : Modification d'une couche n'impacte pas les autres
4. **Scalabilité** : On peut scaler horizontalement (plusieurs instances)

---

## 2. Flux de données détaillés

### 2.1 Flux : Achat d'un jeu (Synchrone)

```
CLIENT (Player Simulator)
  │
  │ HTTP POST /api/purchases
  │ Body: {"userId": "user-123", "gameId": "game-456", "price": 59.99}
  │
  ▼
┌─────────────────────────────────────┐
│   PurchaseController                │
│   @PostMapping("/api/purchases")    │
└─────────────────────────────────────┘
  │
  │ 1. Extraction du body
  │    PurchaseGameRequest request
  │
  ▼
┌─────────────────────────────────────┐
│   PurchaseService                   │
│   purchaseGame(request)             │
└─────────────────────────────────────┘
  │
  │ 2. @Transactional BEGIN
  │
  ▼
┌─────────────────────────────────────┐
│   Validations métier                │
│   - L'utilisateur existe ?          │
│   - Le jeu existe et est disponible?│
│   - L'utilisateur possède déjà ?    │
└─────────────────────────────────────┘
  │
  │ 3. userRepository.findById(userId)
  │    gameRepository.findById(gameId)
  │
  ▼
┌─────────────────────────────────────┐
│   Création Purchase                 │
│   - userId = "user-123"             │
│   - gameId = "game-456"             │
│   - purchaseDate = now()            │
│   - price = 59.99                   │
└─────────────────────────────────────┘
  │
  │ 4. purchaseRepository.save(purchase)
  │    Query: INSERT INTO purchases VALUES (...)
  │
  │ 5. @Transactional COMMIT
  │
  ▼
┌─────────────────────────────────────┐
│   PurchaseController (réponse)      │
│   ResponseEntity.ok(PurchaseModel)  │
└─────────────────────────────────────┘
  │
  │ HTTP 201 CREATED
  │ Body: {"id": "...", "userId": "user-123", ...}
  │
  ▼
CLIENT (reçoit la réponse)
```

**Points clés:**

1. **Transaction ACID** : Les validations et l'insertion sont atomiques
2. **Validation métier** : Empêche les doublons et les achats invalides
3. **Réponse immédiate** : L'utilisateur sait instantanément si l'achat a réussi

---

### 2.2 Flux : Réception d'un nouveau jeu (Asynchrone via Kafka)

```
KAFKA TOPIC: game-released
  │
  │ Message disponible
  │ Clé: "publisher-123"
  │ Valeur: GameReleased (bytes Avro)
  │
  ▼
┌─────────────────────────────────────┐
│   GameReleasedConsumer              │
│   @KafkaListener(topics = "...")    │
└─────────────────────────────────────┘
  │
  │ 1. Désérialisation automatique
  │    GameReleased event
  │
  ▼
┌─────────────────────────────────────┐
│   handleGameReleased(event)         │
└─────────────────────────────────────┘
  │
  │ 2. Conversion Event → Entity
  │    Game game = new Game();
  │    game.setId(event.getGameId());
  │    game.setTitle(event.getTitle());
  │    ...
  │
  ▼
┌─────────────────────────────────────┐
│   GameRepository                    │
│   save(game)                        │
└─────────────────────────────────────┘
  │
  │ 3. Query: INSERT INTO games VALUES (...)
  │    Result: Game sauvegardé
  │
  ▼
┌─────────────────────────────────────┐
│   Logger                            │
│   logger.info("New game added: {}") │
└─────────────────────────────────────┘
  │
  │ 4. Commit offset Kafka automatique
  │
  ▼
En attente du prochain message
```

**Points clés:**

1. **@KafkaListener** : Spring gère automatiquement la boucle de polling
2. **Désérialisation Avro** : Conversion automatique bytes → Java Object
3. **Pas de transaction distribuée** : Kafka et BD sont indépendants
4. **Idempotence** : Si le même event arrive 2 fois, on pourrait vérifier l'existence avant l'insert

---

### 2.3 Flux : Session de jeu (Event-Driven)

```
CLIENT (Player joue à un jeu)
  │
  │ HTTP POST /api/session
  │ Body: {"userId": "user-123", "gameId": "game-456", 
  │        "startTimestamp": 1738000000, "timePlayed": 3600000}
  │
  ▼
┌─────────────────────────────────────┐
│   SessionController                 │
│   @PostMapping("/api/session")      │
└─────────────────────────────────────┘
  │
  │ 1. Validation
  │
  ▼
┌─────────────────────────────────────┐
│   SessionService                    │
│   saveSession(request)              │
└─────────────────────────────────────┘
  │
  │ 2. Création Session
  │    - userId, gameId
  │    - startTimestamp
  │    - duration (timePlayed)
  │
  ▼
┌─────────────────────────────────────┐
│   SessionRepository                 │
│   save(session)                     │
└─────────────────────────────────────┘
  │
  │ 3. Query: INSERT INTO sessions
  │
  ▼
┌─────────────────────────────────────┐
│   Statistiques calculées            │
│   - Temps total de jeu mis à jour   │
│   - Dernière session enregistrée    │
└─────────────────────────────────────┘
  │
  │ HTTP 201 CREATED
  │
  ▼
CLIENT
```

---

## 3. Explication des patterns utilisés

### 3.1 Repository Pattern (Spring Data JPA)

**Problème:** Écrire du SQL manuellement est répétitif et source d'erreurs.

**Solution:** Spring Data JPA génère les implémentations automatiquement.

```java
// INTERFACE (pas d'implémentation !)
public interface UserRepository extends JpaRepository<User, String> {
    
    // CONVENTION DE NOMMAGE = GÉNÉRATION AUTOMATIQUE
    Optional<User> findByUsername(String username);
    // → Spring génère: SELECT * FROM users WHERE username = ?
    
    Optional<User> findByEmail(String email);
    // → Spring génère: SELECT * FROM users WHERE email = ?
    
    boolean existsByUsername(String username);
    // → Spring génère: SELECT EXISTS(SELECT 1 FROM users WHERE username = ?)
    
    @Query("SELECT u FROM User u WHERE u.lastLoginTime > :threshold")
    List<User> findActiveUsers(@Param("threshold") Long threshold);
}
```

**Bénéfices:**
- Pas de SQL manuel → moins d'erreurs
- Protection contre SQL injection
- Changement de BD = 0 ligne de code modifiée

---

### 3.2 Service Layer Pattern

**Problème:** La logique métier ne doit pas être dans les controllers.

**Solution:** Services encapsulent la logique métier.

```java
@Service
@RequiredArgsConstructor
public class PurchaseService {
    private final PurchaseRepository purchaseRepository;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final LibraryService libraryService;
    
    @Transactional
    public PurchaseModel purchaseGame(PurchaseGameRequest request) {
        // 1. VALIDATIONS MÉTIER
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Game game = gameRepository.findById(request.getGameId())
            .orElseThrow(() -> new IllegalArgumentException("Game not found"));
        
        // Vérifier si déjà possédé
        if (libraryService.hasGameInLibrary(user.getId(), game.getId())) {
            throw new IllegalStateException("Game already owned");
        }
        
        // 2. CRÉATION PURCHASE
        Purchase purchase = new Purchase();
        purchase.setUserId(user.getId());
        purchase.setGameId(game.getId());
        purchase.setPurchaseDate(System.currentTimeMillis());
        purchase.setPrice(request.getPrice());
        
        // 3. SAUVEGARDE
        Purchase saved = purchaseRepository.save(purchase);
        
        // 4. AJOUT À LA BIBLIOTHÈQUE
        libraryService.addGameToLibrary(user.getId(), game.getId());
        
        return mapToModel(saved);
    }
}
```

**Pourquoi ?**
- **Réutilisabilité** : Le même service peut être appelé depuis plusieurs controllers
- **Testabilité** : On peut mocker les repositories pour les tests
- **Transactions** : @Transactional au niveau service garantit l'atomicité

---

### 3.3 Dependency Injection (Spring IoC Container)

**Problème:** Création manuelle des dépendances = couplage fort.

```java
// ❌ MAUVAIS (couplage fort)
public class PurchaseService {
    private UserRepository userRepository = new UserRepositoryImpl();
    // → Impossible de tester avec un mock
}
```

**Solution:** Injection par constructeur avec @RequiredArgsConstructor (Lombok).

```java
// ✅ BON (couplage faible)
@Service
@RequiredArgsConstructor // Lombok génère le constructeur
public class PurchaseService {
    private final PurchaseRepository purchaseRepository;
    private final GameRepository gameRepository;
    
    // Spring injecte automatiquement les dépendances
}
```

---

### 3.4 Transaction Pattern (@Transactional)

**Problème:** Garantir la cohérence sur plusieurs opérations.

```java
@Transactional
public PurchaseModel purchaseGame(PurchaseGameRequest request) {
    // 1. INSERT purchase
    purchaseRepository.save(purchase);
    
    // 2. INSERT library entry
    libraryService.addGameToLibrary(userId, gameId);
    
    // Si erreur ici → ROLLBACK complet
    
    // Succès → COMMIT automatique
    return result;
}
```

**ACID Properties:**
- **Atomicity:** Tout ou rien (si erreur, rollback complet)
- **Consistency:** État valide avant et après
- **Isolation:** Transactions concurrentes isolées
- **Durability:** Une fois commité, persisté définitivement

---

## 4. Composants clés expliqués

### 4.1 Kafka Consumers (Event Processing)

```java
@Component
public class GameReleasedConsumer {
    
    @Autowired
    private GameService gameService;
    
    @KafkaListener(
        topics = "game-released",
        groupId = "platform-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleGameReleased(GameReleased event) {
        logger.info("📥 New game released: {}", event.getTitle());
        
        // Conversion Avro → JPA Entity
        Game game = new Game();
        game.setId(event.getGameId());
        game.setTitle(event.getTitle());
        game.setGenre(event.getGenre());
        game.setPrice(event.getPrice());
        game.setReleaseTimestamp(event.getReleaseTimestamp());
        
        // Sauvegarde en base
        gameService.saveGame(game);
        
        logger.info("✅ Game added to catalog: {}", game.getTitle());
    }
}
```

**Fonctionnement:**
1. Spring Kafka crée automatiquement un Consumer
2. Polling automatique toutes les 100ms
3. Désérialisation Avro → Java Object
4. Appel de la méthode annotée
5. Commit offset automatique si succès

---

### 4.2 REST Controllers (API Endpoints)

```java
@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameController {
    
    private final GameService gameService;
    
    @GetMapping
    public ResponseEntity<List<GameModel>> getAllGames() {
        return ResponseEntity.ok(gameService.getAllAvailableGames());
    }
    
    @GetMapping("/{gameId}")
    public ResponseEntity<GameModel> getGame(@PathVariable String gameId) {
        return gameService.getGameById(gameId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<GameModel>> searchGames(@RequestParam String title) {
        return ResponseEntity.ok(gameService.searchGames(title));
    }
    
    @GetMapping("/genre/{genre}")
    public ResponseEntity<List<GameModel>> getGamesByGenre(@PathVariable String genre) {
        return ResponseEntity.ok(gameService.getGamesByGenre(genre));
    }
}
```

**Mapping des endpoints:**
```
GET    /api/games              → Tous les jeux
GET    /api/games/{gameId}     → Détails d'un jeu
GET    /api/games/search?title=Zelda → Recherche
GET    /api/games/genre/Action → Jeux par genre
GET    /api/games/platform/PS5 → Jeux par plateforme
```

---

### 4.3 UserService (Gestion Utilisateurs)

```java
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    
    @Transactional
    public UserModel registerUser(UserRegistrationRequest request) {
        // 1. Validation
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        // 2. Création utilisateur
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(hashPassword(request.getPassword())); // À implémenter
        user.setRegistrationDate(System.currentTimeMillis());
        
        // 3. Sauvegarde
        User saved = userRepository.save(user);
        
        return mapToModel(saved);
    }
    
    public Optional<UserModel> getUserById(String userId) {
        return userRepository.findById(userId)
            .map(this::mapToModel);
    }
    
    public Optional<UserModel> getUserByUsername(String username) {
        return userRepository.findByUsername(username)
            .map(this::mapToModel);
    }
}
```

---

### 4.4 NotificationsService (Push Notifications)

```java
@Service
@RequiredArgsConstructor
public class NotificationsService {
    
    private final NotificationRepository notificationRepository;
    
    public Notification createNotification(String userId, String description) {
        Notification notif = new Notification();
        notif.setId(UUID.randomUUID().toString());
        notif.setUserId(userId);
        notif.setDescription(description);
        notif.setTimestamp(System.currentTimeMillis());
        notif.setRead(false);
        
        return notificationRepository.save(notif);
    }
    
    public List<NotificationModel> getUserNotifications(String userId) {
        return notificationRepository.findByUserId(userId).stream()
            .map(this::mapToModel)
            .collect(Collectors.toList());
    }
}
```

**Utilisé par les consumers Kafka pour notifier les utilisateurs:**
- Nouveau patch disponible
- Nouveau DLC disponible
- Ami a acheté un jeu
- Éditeur suivi a publié un jeu

---

## 5. Gestion des erreurs et résilience

### 5.1 Global Exception Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage(),
            System.currentTimeMillis()
        );
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.CONFLICT.value(),
            ex.getMessage(),
            System.currentTimeMillis()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal server error",
            System.currentTimeMillis()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

---

### 5.2 Kafka Consumer Error Handling

```java
@KafkaListener(topics = "game-released", groupId = "platform-service")
public void handleGameReleased(GameReleased event) {
    try {
        gameService.saveGame(convertToEntity(event));
        logger.info("✅ Game processed: {}", event.getTitle());
    } catch (DataAccessException ex) {
        // Erreur BD → Log + Retry (Kafka redelivery)
        logger.error("❌ Database error: {}", ex.getMessage());
        throw ex; // Kafka va retry
    } catch (Exception ex) {
        // Erreur inattendue → Log + Skip (commit offset)
        logger.error("⚠️ Unexpected error, skipping: {}", ex.getMessage());
        // Pas de throw → message est considéré comme traité
    }
}
```

---

## 6. Performance et optimisations

### 6.1 Index de base de données

```java
@Entity
@Table(
    name = "games",
    indexes = {
        @Index(name = "idx_game_title", columnList = "title"),
        @Index(name = "idx_game_genre", columnList = "genre"),
        @Index(name = "idx_game_platform", columnList = "platform")
    }
)
public class Game {
    @Id
    private String id;
    
    @Column(nullable = false)
    private String title;
    
    private String genre;
    private String platform;
    // ...
}
```

**Impact:**
- Recherche par titre : **O(log n)** au lieu de O(n)
- Filtrage par genre/plateforme : **1000x plus rapide**

---

### 6.2 Lazy Loading (JPA)

```java
@Entity
public class User {
    @Id
    private String id;
    
    // Chargement LAZY par défaut pour les collections
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Purchase> purchases;
    
    // Les purchases ne sont chargées que si on y accède
}
```

**Bénéfices:**
- Ne charge pas les données inutiles
- Réduit la mémoire et le temps de requête

---

### 6.3 Kafka Consumer Group

```
Topic game-released (3 partitions)
Consumer Group "platform-service" (3 instances)

Instance 1 → Partition 0
Instance 2 → Partition 1
Instance 3 → Partition 2

Throughput: 3x plus élevé (parallélisation automatique)
```

---

## 🎓 Conclusion

Le Platform Service est le **cœur de la plateforme** avec :

✅ **Architecture REST** - API claire et documentée  
✅ **Event-Driven** - Réception de events Kafka pour synchronisation  
✅ **CRUD complet** - Gestion utilisateurs, jeux, achats, sessions  
✅ **Services métier** - Logique encapsulée et réutilisable  
✅ **Transactions ACID** - Cohérence des données garantie  
✅ **Scalabilité** - Kafka consumers parallélisés  
✅ **Sécurité** - Validation métier robuste  

**Endpoints principaux:**
- `/api/games` - Catalogue de jeux
- `/api/users` - Gestion utilisateurs
- `/api/purchases` - Achats de jeux
- `/api/library` - Bibliothèque utilisateur
- `/api/session` - Sessions de jeu
- `/api/wishlist` - Liste de souhaits
- `/api/reviews` - Avis et notes
- `/api/notifications` - Notifications push
