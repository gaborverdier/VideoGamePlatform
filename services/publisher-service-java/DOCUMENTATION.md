# 📚 DOCUMENTATION COMPLÈTE - Publisher Service
**Dernière mise à jour:** 2025-12-28
**Version:** 1.0.0  
**Auteur:** Publisher Service Team  

---

- **Confluent Schema Registry:** https://docs.confluent.io/platform/current/schema-registry/
- **Avro:** https://avro.apache.org/docs/current/
- **Apache Kafka:** https://kafka.apache.org/documentation/
- **Spring Boot:** https://spring.io/projects/spring-boot

## 📚 Références

---

Si vide, les schémas Avro ne sont pas enregistrés. Solution: générer les classes Avro.

```
curl http://localhost:8081/subjects
```bash
**Vérification:**

### Problème: Schema Registry error

```
docker-compose up -d
cd docker
```bash

**Solution:** Démarrer Kafka via Docker

### Problème: Kafka connection refused

```
curl -X POST http://localhost:8082/api/admin/reload-vgsales
# Ou recharger manuellement via API
```bash

**Solution:** Vérifier que `vgsales.csv` existe dans `./data/`

### Problème: Pas de jeux en base

## 🐛 Troubleshooting

---

```
publisher.vgsales.auto-load=true
publisher.vgsales.path=./data/vgsales.csv
publisher.crash-threshold=10
publisher.name=Activision
# Business

kafka.topic.game-crash-reported=game-crash-reported
kafka.topic.game-patched=game-patched
# Topics

kafka.schema.registry.url=http://localhost:8081
kafka.bootstrap.servers=localhost:9092
# Kafka

spring.jpa.hibernate.ddl-auto=update  # Crée/met à jour le schéma auto
spring.datasource.url=jdbc:h2:file:./data/publisher-db
# Base de données
```properties

## ⚙️ Configuration (application.properties)

---

```
→ Load balancing automatique !

Instance 3: Lit partition 2
Instance 2: Lit partition 1
Instance 1: Lit partition 0

Consumer Group: publisher-service-group
Topic: game-crash-reported (3 partitions)
```

### 4. Consumer Group Kafka

```
}
    // Si une erreur survient, ROLLBACK automatique
    
    patchHistoryRepository.save(patch);  // INSERT
    
    gameRepository.save(game);
    game.setCurrentVersion(newVersion);  // UPDATE
    // Tout ce code est dans une transaction
public PatchHistory deployPatch(String gameId, String changelog) {
@Transactional
```java

### 3. Transaction JPA

Kafka assigne un ID unique et déduplique automatiquement.

```
enable.idempotence = true
```java
**Solution:**

```
Producer retry → Message envoyé 2 fois (doublon)
Producer envoie message → Network error
```
**Problème sans idempotence:**

### 2. Idempotence Kafka

```
}
    return String.format("%d.%d.%d", major, minor, patch);
    patch++;  // 1.2.3 → 1.2.4
    int patch = Integer.parseInt(parts[2]);
    String[] parts = currentVersion.split("\\.");
private String incrementVersion(String currentVersion) {
```java
**Dans le code:**

```
PATCH: Bug fixes
MINOR: New features (backward compatible)
MAJOR: Breaking changes

  1  .  2  .  3
MAJOR.MINOR.PATCH
```

### 1. Semantic Versioning

## 🎓 Concepts Clés Expliqués

---

```
└─────────────────┘
│ time_window_end │
│ time_window_start│
│ aggregation_ts  │
│ total_ratings   │
│ average_rating  │
│ game_id (FK)    │
│ id (PK)         │
├─────────────────┤
│ REVIEW_STATS    │
┌─────────────────┐

└─────────────────┘
│ user_id         │
│ crash_timestamp │
│ game_version    │
│ platform        │
│ stack_trace     │
│ error_message   │
│ error_code      │
│ game_id (FK)    │
│ crash_id (PK)   │
├─────────────────┤
│ CRASH_REPORTS   │
┌─────────────────┐

└─────────────────┘
│ release_date    │
│ patch_size      │
│ changelog       │
│ previous_version│
│ version         │
│ game_id (FK)    │
│ id (PK)         │
├─────────────────┤
│ PATCH_HISTORY   │
┌─────────────────┐
        ↓
        │ 1:N
        │
└─────────────────┘
│ updated_at      │
│ created_at      │
│ description     │
│ current_version │
│ publisher       │
│ platform        │
│ genre           │
│ title           │
│ id (PK)         │
├─────────────────┤
│     GAMES       │
┌─────────────────┐
```

## 📊 Schéma de Base de Données

---

- Password: *(vide)*
- User: `sa`
- JDBC URL: `jdbc:h2:file:./data/publisher-db`
- URL: `http://localhost:8082/h2-console`

### Console H2

- Info: `http://localhost:8082/actuator/info`
- Metrics: `http://localhost:8082/actuator/metrics`
- Health: `http://localhost:8082/actuator/health`

### Spring Actuator

```
logger.error("❌ CRITIQUE : Note de 1.2/5");
logger.warn("⚠️ ALERTE : 15 crashs détectés (seuil: 10)");
logger.info("✅ Patch publié avec succès");
```java

### Logs SLF4J

## 🔍 Monitoring & Observabilité

---

```
}
  "totalReviews": 8
  "totalCrashes": 12,
  "totalPatches": 45,
  "totalGames": 150,
{
```json
**Réponse:**

```
curl http://localhost:8082/api/admin/stats
```bash

### Statistiques globales

```
  -d '{"genre": "Action-RPG", "description": "Epic adventure game"}'
  -H "Content-Type: application/json" \
curl -X PUT http://localhost:8082/api/games/{gameId}/metadata \
```bash

### Mettre à jour métadonnées

```
  -d '{"changelog": "- Fixed critical bug\n- Improved performance"}'
  -H "Content-Type: application/json" \
curl -X POST http://localhost:8082/api/games/{gameId}/patch \
```bash

### Publier un patch

## 🧪 Exemples d'utilisation (cURL)

---

| GET | `/api/admin/stats` | Statistiques globales |
| POST | `/api/admin/simulate-patch` | Déclenche une simulation |
| POST | `/api/admin/reload-vgsales` | Recharge les données VGSales |
|---------|----------|-------------|
| Méthode | Endpoint | Description |

### ADMIN

| GET | `/api/reviews/game/{id}` | Stats d'un jeu |
| GET | `/api/reviews` | Liste toutes les stats de notes |
|---------|----------|-------------|
| Méthode | Endpoint | Description |

### REVIEWS

| GET | `/api/crashes/stats` | Statistiques globales |
| GET | `/api/crashes/game/{id}` | Crashes d'un jeu |
| GET | `/api/crashes` | Liste tous les crashes |
|---------|----------|-------------|
| Méthode | Endpoint | Description |

### CRASHES

| PUT | `/api/games/{id}/metadata` | Met à jour métadonnées | `{"genre": "Action", "platform": "PS5"}` |
|---------|----------|-------------|------|
| Méthode | Endpoint | Description | Body |

### METADATA

| GET | `/api/games/{id}/patches` | Historique des patches | - |
| POST | `/api/games/{id}/patch` | Publie un patch | `{"changelog": "..."}` |
|---------|----------|-------------|------|
| Méthode | Endpoint | Description | Body |

### PATCHES

| GET | `/api/games/search?title=zelda` | Recherche par titre |
| GET | `/api/games/{id}` | Détails d'un jeu |
| GET | `/api/games` | Liste tous les jeux |
|---------|----------|-------------|
| Méthode | Endpoint | Description |

### GAMES

## 🎮 API REST Complète

---

**But:** Générer du trafic Kafka pour démonstration

3. Appelle `patchService.deployPatch()`
2. Génère un changelog aléatoire
1. Sélectionne un jeu aléatoire
**Logique:**

**Tâche planifiée:** `@Scheduled(fixedDelay = 120000)` (toutes les 2 minutes)

### AutoPatchSimulatorService

4. Sauvegarde en batch
3. Évite les doublons (vérifie `existsByTitle`)
2. Filtre selon `publisher.name` (config)
1. Lit le fichier ligne par ligne
**Parsing:**

```
Wii Sports,Wii,2006,Sports,Nintendo,41.49,29.02,3.77,8.46,82.74
Name,Platform,Year,Genre,Publisher,NA_Sales,EU_Sales,JP_Sales,Other_Sales,Global_Sales
```csv
**Format CSV attendu:**

**Méthode principale:** `loadGamesFromCSV()`

### VGSalesLoaderService

- Publie GameMetadataUpdatedEvent si changement détecté
- Met à jour uniquement les champs modifiés
- Paramètres `null` = pas de modification
**Logique:**

**Méthode principale:** `updateMetadata(String gameId, String genre, String platform, String description)`

### MetadataService

**Transaction:** Tout est atomique (rollback si erreur)

```
5. Publie GamePatchedEvent sur Kafka
4. Crée l'entrée PatchHistory
3. Met à jour game.currentVersion
   Exemple: 1.2.3 → 1.2.4
2. Calcule la nouvelle version (semantic versioning)
1. Récupère le jeu en base
```
**Workflow:**

**Méthode principale:** `deployPatch(String gameId, String changelog)`

### PatchService

## 🚀 Services Métier

---

- **Repository:** Requêtes SQL
- **Service:** Logique métier, transactions
- **Controller:** Validation HTTP, sérialisation JSON
**Séparation des responsabilités:**

```
Database
    ↓
Repository (Accès données)
    ↓
Service (Logique métier)
    ↓
Controller (API REST)
```

### 3. **Service Layer Pattern**

- Abstraction de la couche d'accès aux données
- Transactions automatiques
- Pas de SQL manuel
**Avantage:**

```
}
    List<Game> findByPublisher(String publisher);
    Optional<Game> findByTitle(String title);
    // Spring génère automatiquement l'implémentation !
public interface GameRepository extends JpaRepository<Game, String> {
```java

### 2. **Repository Pattern** (Spring Data JPA)

- Évite 4-5 classes dupliquées
- Maintenance facilitée (un seul endroit à modifier)
- Code de production Kafka écrit une seule fois
**Avantage DRY:**

```
}
    // Hérite de sendAsync, pas de duplication de code
public class GamePatchedProducer extends BaseKafkaProducer<GamePatchedEvent> {
@Component
// Implémentation spécifique

}
    }
        producer.send(record, callback);
        ProducerRecord<String, T> record = new ProducerRecord<>(topicName, key, event);
    public void sendAsync(String key, T event) {
    // Méthode commune à tous les producteurs
    
    protected KafkaProducer<String, T> producer;
public abstract class BaseKafkaProducer<T> {
// Classe de base abstraite
```java

### 1. **Template Method Pattern** (BaseKafkaProducer)

## 🧩 Pattern de Conception Utilisés

---

```
}
    auto.offset.reset = "earliest"  // Lire depuis le début si nouveau groupe
    enable.auto.commit = true
    // Commit automatique
    
    specific.avro.reader = true
    // Utiliser les classes Avro générées (pas GenericRecord)
    
    value.deserializer = KafkaAvroDeserializer
    key.deserializer = StringDeserializer
    // Désérialisation
    
    group.id = "publisher-service-group"
    bootstrap.servers = "localhost:9092"
    // Connexion Kafka
public Map<String, Object> consumerConfigs() {
@Bean
```java

### Consommateur Kafka

```
}
    schema.registry.url = "http://localhost:8081"
    // Schema Registry
    
    enable.idempotence = true       // Éviter les doublons
    retries = 3                     // Réessayer en cas d'erreur
    acks = "all"                    // Attendre tous les réplicas
    // Fiabilité
    
    value.serializer = KafkaAvroSerializer (Avro + Schema Registry)
    key.serializer = StringSerializer
    // Sérialisation
    
    bootstrap.servers = "localhost:9092"
    // Connexion Kafka
public Map<String, Object> producerConfigs() {
@Bean
```java

**Principe DRY:** Configuration centralisée réutilisée par tous les producteurs

### Producteur Kafka

## 🎛️ Configuration Kafka (KafkaConfig.java)

---

2. Analyse la tendance qualité (✅ Excellent / ⚠️ Attention / ❌ Critique)
1. Sauvegarde en base `ReviewStats`
**Traitement:**
**Topic:** `game-rating-aggregated`  

```
}
  "timeWindowEnd": 1703779200000
  "timeWindowStart": 1703692800000,
  "aggregationTimestamp": 1703779200000,
  "ratingDistribution": {"5": 1200, "4": 200, "3": 100},
  "totalRatings": 1523,
  "averageRating": 4.8,
  "gameTitle": "Zelda BOTW",
  "gameId": "uuid-123",
{
```json
#### 4. GameRatingAggregatedEvent

3. Log une alerte si dépassement
2. Vérifie le seuil d'alerte (config: `publisher.crash-threshold`)
1. Sauvegarde en base `CrashReport`
**Traitement:**
**Topic:** `game-crash-reported`  

```
}
  "userId": "user-789"
  "crashTimestamp": 1703779200000,
  "gameVersion": "1.2.3",
  "platform": "Nintendo Switch",
  "stackTrace": "java.lang.OutOfMemoryError...",
  "errorMessage": "Out of memory in level 5",
  "errorCode": "ERR_MEMORY_LEAK",
  "gameTitle": "Zelda BOTW",
  "gameId": "uuid-123",
  "crashId": "crash-456",
{
```json
#### 3. GameCrashReportedEvent

### **Événements CONSOMMÉS** (Kafka → Publisher)

---

**Déclencheur:** Appel API `PUT /api/games/{id}/metadata`
**Topic:** `game-metadata-updated`  

```
}
  "publisher": "Nintendo"
  "updateTimestamp": 1703779200000,
  "description": "Open-world adventure game",
  "platform": "Nintendo Switch",
  "genre": "Action-Adventure",
  "gameTitle": "Zelda BOTW",
  "gameId": "uuid-123",
{
```json
#### 2. GameMetadataUpdatedEvent

**Déclencheur:** Appel API `POST /api/games/{id}/patch` ou simulation automatique
**Topic:** `game-patched`  

```
}
  "publisher": "Nintendo"
  "releaseTimestamp": 1703779200000,
  "patchSize": 150000000,
  "changelog": "- Fixed memory leak\n- Improved graphics",
  "previousVersion": "1.2.3",
  "version": "1.2.4",
  "gameTitle": "Zelda BOTW",
  "gameId": "uuid-123",
{
```json
#### 1. GamePatchedEvent

### **Événements PRODUITS** (Publisher → Kafka)

## 🔄 Flux de Données Kafka

---

```
}
    LocalDateTime timeWindowEnd;
    LocalDateTime timeWindowStart;
    LocalDateTime aggregationTimestamp;
    Long totalRatings;      // Nombre total de votes
    Double averageRating;   // Note moyenne (0.0 - 5.0)
    String gameId;
    String id;
public class ReviewStats {
@Table(name = "review_stats")
@Entity
```java

### 4. **ReviewStats** - Statistiques de notes

```
}
    String userId;          // Utilisateur affecté
    LocalDateTime crashTimestamp;
    String gameVersion;     // Version lors du crash
    String platform;        // Plateforme du crash
    String stackTrace;      // Stack trace complet
    String errorMessage;    // Message détaillé
    String errorCode;       // Code d'erreur (ERR_MEMORY_LEAK)
    String gameId;          // Jeu concerné
    String crashId;         // ID unique du crash
public class CrashReport {
@Table(name = "crash_reports")
@Entity
```java

### 3. **CrashReport** - Rapport de crash

```
}
    LocalDateTime releaseDate;
    Long patchSize;         // Taille en octets
    String changelog;       // Description des changements
    String previousVersion; // Version précédente (1.2.3)
    String version;         // Nouvelle version (1.2.4)
    String gameId;          // Référence au jeu
    String id;              // UUID
public class PatchHistory {
@Table(name = "patch_history")
@Entity
```java

### 2. **PatchHistory** - Historique des patches

- `@PreUpdate`: Met à jour `updatedAt` automatiquement
- `@PrePersist`: Initialise `createdAt`, `updatedAt` et `currentVersion` à "1.0.0"
**Lifecycle Hooks:**

```
}
    LocalDateTime updatedAt;
    LocalDateTime createdAt;
    String description;     // Description
    String currentVersion;  // Version actuelle (ex: 1.2.3)
    String publisher;       // Nom de l'éditeur
    String platform;        // Plateforme (PS5, Xbox, PC)
    String genre;           // Genre (Action, RPG, etc.)
    String title;           // Titre du jeu
    String id;              // UUID généré automatiquement
public class Game {
@Table(name = "games")
@Entity
```java

### 1. **Game** - Représente un jeu vidéo

## 📦 Modèle de Données (Entités JPA)

---

```
└── service/             # Logique métier
├── repository/          # Accès aux données (Spring Data)
├── producer/            # Producteurs Kafka
├── model/               # Entités JPA (base de données)
├── dto/                 # Data Transfer Objects (événements)
├── consumer/            # Consommateurs Kafka
├── controller/          # REST API endpoints
├── config/              # Configuration Spring & Kafka
com.gaming.publisher/
```

### Structure des packages

## 🏗️ Architecture du Code

---

Le **Publisher Service** simule le comportement d'un éditeur de jeux vidéo dans un écosystème de plateforme de gaming. Il gère un catalogue de jeux, publie des patches, met à jour des métadonnées, et analyse les rapports de crash et les statistiques de qualité.

## 🎯 Vue d'ensemble


