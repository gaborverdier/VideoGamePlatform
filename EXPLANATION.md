# 🎮 GUIDE D'EXPLICATION DÉTAILLÉ - VideoGamePlatform

Ce document explique en détail **comment fonctionne** l'architecture globale du projet VideoGamePlatform, une plateforme de jeux vidéo event-driven complète.

---

## 📚 Table des Matières

1. [Vue d'ensemble de l'architecture](#1-vue-densemble-de-larchitecture)
2. [Flux de données globaux](#2-flux-de-données-globaux)
3. [Communication inter-services](#3-communication-inter-services)
4. [Schémas Avro et évolution](#4-schémas-avro-et-évolution)
5. [Infrastructure Docker](#5-infrastructure-docker)
6. [Scalabilité et déploiement](#6-scalabilité-et-déploiement)

---

## 1. Vue d'ensemble de l'architecture

### 1.1 Architecture globale (Event-Driven)

```
┌────────────────────────────────────────────────────────────────────────────┐
│                         VIDEO GAME PLATFORM                                 │
└────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────┐         ┌─────────────────────┐
│  Player Simulator   │         │ Publisher Service   │
│   (JavaFX Client)   │         │  (JavaFX + REST)    │
│                     │         │                     │
│  - Achète des jeux  │         │  - Publie des jeux  │
│  - Joue aux jeux    │         │  - Crée des patches │
│  - Rapporte crashs  │         │  - Crée des DLC     │
│  - Laisse des avis  │         │  - Voit les crashs  │
└──────────┬──────────┘         └──────────┬──────────┘
           │                               │
           │ REST API                      │ REST API + Events
           │                               │
           ▼                               ▼
┌─────────────────────────────────────────────────────────────┐
│              Platform Service (REST API)                    │
│                                                             │
│  - Catalogue de jeux                                        │
│  - Gestion utilisateurs                                     │
│  - Achats et bibliothèque                                   │
│  - Sessions de jeu                                          │
│  - Reviews et wishlist                                      │
│  - Notifications                                            │
│                                                             │
│  Database: PostgreSQL (users, games, purchases, sessions)  │
└─────────────────────────────────────────────────────────────┘
           │                               │
           │ Produit Events                │ Consomme Events
           │                               │
           ▼                               ▼
┌────────────────────────────────────────────────────────────────────────┐
│                        APACHE KAFKA (Event Bus)                         │
│                                                                         │
│  Topics:                                                                │
│  - game-released              : Nouveau jeu publié                      │
│  - game-patch-released        : Nouveau patch disponible               │
│  - dlc-created               : Nouveau DLC créé                        │
│  - game-crash-reported        : Crash de jeu rapporté                  │
│  - game-session-started       : Session de jeu démarrée                │
│  - game-session-ended         : Session de jeu terminée                │
│  - game-reviewed             : Avis/note publié                        │
│  - crash-aggregated          : Agrégation de crashs (1 min)           │
│  - game-popularity-score     : Score de popularité calculé            │
│                                                                         │
│  Schema Registry: Validation Avro des événements                       │
└────────────────────────────────────────────────────────────────────────┘
           │                               │
           │ Consomme Events               │ Produit Aggregations
           │                               │
           ▼                               ▼
┌─────────────────────────────────────────────────────────────┐
│         Analytics Service (Kafka Streams)                   │
│                                                             │
│  - CrashAggregationTopology                                 │
│    → Compte crashs par jeu (fenêtres 1 min)                │
│                                                             │
│  - PopularityScoreTopology                                  │
│    → Score = (reviews × rating) - (crashs × 10)            │
│                                                             │
│  State Stores: RocksDB local                                │
└─────────────────────────────────────────────────────────────┘
```

---

### 1.2 Principes architecturaux

**1. Event-Driven Architecture (EDA)**
- Les services communiquent via événements asynchrones
- Découplage fort : un service peut être down sans bloquer les autres
- Traçabilité : chaque événement est enregistré dans Kafka

**2. CQRS (Command Query Responsibility Segregation)**
- **Commands** : REST API pour écriture (POST, PUT, DELETE)
- **Queries** : REST API pour lecture (GET)
- **Events** : Kafka pour synchronisation asynchrone

**3. Microservices**
- Chaque service a sa propre base de données (Database per Service)
- Autonomie et scalabilité indépendante
- Pas de transactions distribuées (eventual consistency)

**4. Schema Registry (Avro)**
- Contrat d'interface entre services
- Évolution des schémas sans breaking changes
- Validation automatique des messages

---

## 2. Flux de données globaux

### 2.1 Flux : Publication d'un jeu

```
1. Publisher (JavaFX) clique "Publier un jeu"
   │
   ├─ Game: "The Legend of Zelda"
   ├─ Genre: "Action/Adventure"
   ├─ Price: 59.99€
   └─ Platform: "Switch"
   ↓
2. POST /api/games (Publisher Service REST API)
   ↓
3. Publisher Service
   ├─ Validation métier
   ├─ Sauvegarde en PostgreSQL (publisher DB)
   └─ Production Event Kafka
   ↓
4. Kafka Topic: "game-released"
   Event: GameReleased {
     gameId: "game-123",
     title: "The Legend of Zelda",
     genre: "Action/Adventure",
     price: 59.99,
     publisherId: "pub-456",
     releaseTimestamp: 1738000000
   }
   ↓
5. Platform Service (Consumer)
   ├─ Consomme event "game-released"
   ├─ Sauvegarde en PostgreSQL (platform DB)
   └─ Jeu disponible dans le catalogue
   ↓
6. Player Simulator (REST API)
   GET /api/games
   ← Liste des jeux incluant "The Legend of Zelda"
```

**Points clés:**
- **Asynchrone** : Publisher Service ne bloque pas
- **Eventual Consistency** : Le jeu apparaît dans Platform Service après quelques millisecondes
- **Idempotence** : Si l'event arrive 2 fois, pas de doublon (check par gameId)

---

### 2.2 Flux : Achat et session de jeu

```
1. Player (JavaFX) achète "Zelda"
   ↓
2. POST /api/purchases (Platform Service)
   Body: {"userId": "user-789", "gameId": "game-123", "price": 59.99}
   ↓
3. Platform Service
   ├─ Transaction BEGIN
   ├─ INSERT purchases
   ├─ INSERT library (user_id, game_id)
   ├─ Transaction COMMIT
   └─ HTTP 201 CREATED
   ↓
4. Player clique "Jouer"
   ↓
5. PlayerDashboardController.startGame()
   ├─ sessionId = UUID.randomUUID()
   ├─ sessionStartTime = now()
   └─ Production Event Kafka
   ↓
6. Kafka Topic: "game-session-started"
   Event: GameSessionStarted {
     sessionId: "session-abc",
     gameId: "game-123",
     userId: "user-789",
     startTimestamp: 1738001000
   }
   ↓
7. Player joue 45 minutes puis arrête
   ↓
8. PlayerDashboardController.stopGame()
   ├─ duration = now() - sessionStartTime
   ├─ POST /api/session (sauvegarde en DB)
   └─ Production Event Kafka
   ↓
9. Kafka Topic: "game-session-ended"
   Event: GameSessionEnded {
     sessionId: "session-abc",
     gameId: "game-123",
     userId: "user-789",
     endTimestamp: 1738003700,
     duration: 2700000  // 45 min en ms
   }
   ↓
10. Analytics Service (Kafka Streams)
    └─ Peut agréger les sessions pour statistiques
```

---

### 2.3 Flux : Crash d'un jeu

```
1. Player joue à Zelda
   ↓
2. Jeu crash (simulation)
   PlayerDashboardController.reportCrash()
   ↓
3. Kafka Topic: "game-crash-reported"
   Event: GameCrashReported {
     crashId: "crash-def",
     gameId: "game-123",
     userId: "user-789",
     crashCode: 1,  // Graphics error
     crashMessage: "Texture flickering",
     crashTimestamp: 1738002000,
     gameVersion: "1.0.0"
   }
   ↓
4. Analytics Service (Kafka Streams)
   CrashAggregationTopology
   ├─ Fenêtre Tumbling 1 minute
   ├─ Compte crashs par gameId
   └─ Production Event agrégé
   ↓
5. Kafka Topic: "crash-aggregated"
   Event: CrashAggregationModel {
     id: "game-123-1738002000",
     gameId: "game-123",
     crashCount: 15,  // 15 crashs dans la fenêtre
     timestamp: 1738002060,
     windowStart: 1738002000,
     windowEnd: 1738002060
   }
   ↓
6. Publisher Service (Consumer)
   CrashAggregationConsumer
   ├─ Consomme event "crash-aggregated"
   ├─ Sauvegarde en PostgreSQL
   └─ Si crashCount > 10 → Alerte dans UI
   ↓
7. Publisher Dashboard (JavaFX)
   NotificationsTab affiche:
   "⚠️ ALERTE: Le jeu 'Zelda' a 15 crashs (seuil: 10)"
```

**Points clés:**
- **Stream Processing** : Analytics Service traite en temps réel
- **Windowing** : Agrégation par fenêtre de 1 minute
- **Alerting** : Publisher est notifié en quasi temps réel

---

### 2.4 Flux : Publication d'un patch

```
1. Publisher voit l'alerte de crashs
   ↓
2. Publisher clique "Publier un patch"
   PublishPatchDialog s'ouvre
   ├─ Version: 1.0.0 → 1.0.1
   ├─ Changelog: "Fixed texture flickering bug"
   └─ Patch size: 150 MB (simulé)
   ↓
3. POST /api/patch/create (Publisher Service)
   Body: PatchModel {...}
   ↓
4. Publisher Service
   ├─ Validation (jeu existe ?)
   ├─ Sauvegarde Patch en DB
   └─ Production Event Kafka
   ↓
5. Kafka Topic: "game-patch-released"
   Event: PatchModel {
     id: "patch-ghi",
     gameId: "game-123",
     version: "1.0.1",
     changelog: "Fixed texture flickering bug",
     releaseTimestamp: 1738003000,
     patchSize: 157286400
   }
   ↓
6. Platform Service (Consumer)
   PatchReleasedConsumer
   ├─ Consomme event "game-patch-released"
   ├─ Met à jour Game.version en DB
   └─ Crée Notification pour utilisateurs
   ↓
7. Platform Service
   NotificationsService.createNotification()
   └─ Pour chaque utilisateur possédant le jeu:
       INSERT notification:
       "Le jeu 'Zelda' a une nouvelle version 1.0.1 disponible !"
   ↓
8. Player Simulator (REST API)
   GET /api/notifications/user/{userId}
   ← Liste des notifications incluant le patch
   ↓
9. Player Dashboard (JavaFX)
   NotificationsTab affiche:
   "🎮 Le jeu 'The Legend of Zelda' a une nouvelle version 1.0.1 !"
```

---

## 3. Communication inter-services

### 3.1 Patterns de communication

**Synchrone (REST API):**
```
Player Simulator ──HTTP GET──> Platform Service
                              ↓
                          Response JSON
                              ↓
Player Simulator <──────────┘
```

**Asynchrone (Kafka Events):**
```
Publisher Service ──Event──> Kafka Topic
                              ↓
                         (message persisted)
                              ↓
Platform Service <──Poll───┘ (when ready)
```

**Comparaison:**

| Aspect | REST (Synchrone) | Kafka (Asynchrone) |
|--------|-----------------|-------------------|
| Latence | Faible (<50ms) | Moyenne (100-500ms) |
| Couplage | Fort (service doit être UP) | Faible (découplé) |
| Fiabilité | Retry manuel | Retry automatique |
| Scalabilité | Limitée | Excellente |
| Use case | Queries, Commands urgents | Events, Notifications |

---

### 3.2 Topologie des événements

```
┌──────────────────────┐
│  Publisher Service   │
└──────────┬───────────┘
           │ Produit
           ▼
     ┌──────────────┐
     │ game-released│
     └──────┬───────┘
            │ Consomme
            ▼
     ┌──────────────┐
     │Platform Srv  │
     └──────────────┘

┌──────────────────────┐
│  Player Simulator    │
└──────────┬───────────┘
           │ Produit
           ▼
     ┌────────────────────┐
     │game-crash-reported │
     └──────┬─────────────┘
            │ Consomme
            ▼
     ┌──────────────┐
     │Analytics Srv │
     └──────┬───────┘
            │ Produit
            ▼
     ┌──────────────┐
     │crash-aggregated│
     └──────┬───────┘
            │ Consomme
            ▼
     ┌──────────────┐
     │Publisher Srv │
     └──────────────┘
```

---

### 3.3 Consumer Groups

```
Topic: game-crash-reported (3 partitions)

┌─────────────────────────────────────────┐
│  Consumer Group: analytics-service       │
│                                          │
│  Instance 1 → Partition 0                │
│  Instance 2 → Partition 1                │
│  Instance 3 → Partition 2                │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│  Consumer Group: platform-service        │
│                                          │
│  Instance 1 → Partitions 0, 1, 2        │
└─────────────────────────────────────────┘
```

**Avantages:**
- Chaque consumer group reçoit **tous** les messages
- Parallélisation au sein d'un groupe
- Scalabilité indépendante

---

## 4. Schémas Avro et évolution

### 4.1 Structure du module Avro

```
common/avro-schemas/
├─ build.gradle.kts
├─ settings.gradle.kts
└─ src/main/avro/
   ├─ GameReleased.avsc
   ├─ GameCrashReported.avsc
   ├─ GameSessionStarted.avsc
   ├─ GameReviewed.avsc
   └─ ...

Compilation Gradle:
  avro → Java classes générées → JAR → Dépendance partagée
```

**Exemple de schéma:**
```json
{
  "type": "record",
  "name": "GameCrashReported",
  "namespace": "com.gaming.events",
  "fields": [
    {"name": "crashId", "type": "string"},
    {"name": "gameId", "type": "string"},
    {"name": "userId", "type": "string"},
    {"name": "crashCode", "type": "int"},
    {"name": "crashMessage", "type": "string"},
    {"name": "crashTimestamp", "type": "long"},
    {"name": "gameVersion", "type": "string"}
  ]
}
```

---

### 4.2 Évolution des schémas

**Règles de compatibilité:**

1. **BACKWARD** (par défaut)
   - Nouveaux consumers peuvent lire anciens messages
   - Ajout de champs avec valeur par défaut
   ```json
   // Ajout compatible:
   {"name": "platform", "type": "string", "default": "PC"}
   ```

2. **FORWARD**
   - Anciens consumers peuvent lire nouveaux messages
   - Suppression de champs

3. **FULL**
   - BACKWARD + FORWARD
   - Ajout/Suppression avec defaults

**Exemple d'évolution:**
```
Version 1:
GameReleased {
  gameId, title, genre, price
}

Version 2 (BACKWARD compatible):
GameReleased {
  gameId, title, genre, price,
  platform: "PC"  // ← Nouveau champ avec default
}

→ Ancien consumer peut lire V2 (ignore platform)
→ Nouveau consumer peut lire V1 (utilise default "PC")
```

---

### 4.3 Workflow de changement de schéma

```
1. Modifier GameReleased.avsc
   └─ Ajouter champ "dlcCount" avec default: 0

2. cd common/avro-schemas
   .\gradlew publishToMavenLocal

3. Schema Registry valide la compatibilité
   ✅ BACKWARD compatible

4. Services dépendants (optionnel):
   .\gradlew clean build
   → Recompilation avec nouvelle version du schéma

5. Déploiement rolling:
   - Déployer nouveaux consumers (peuvent lire V1 et V2)
   - Déployer nouveaux producers (envoient V2)
   - Pas de downtime !
```

---

## 5. Infrastructure Docker

### 5.1 Services Docker

```yaml
services:
  # Kafka Broker (KRaft mode - sans Zookeeper)
  kafka:
    image: confluentinc/cp-kafka:7.8.3
    ports:
      - "9092:9092"
    environment:
      KAFKA_NODE_ID: 1
      KAFKA_PROCESS_ROLES: broker,controller
      KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9092,CONTROLLER://0.0.0.0:9093
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_CONTROLLER_QUORUM_VOTERS: 1@localhost:9093
      KAFKA_LOG_DIRS: /var/lib/kafka/data
      CLUSTER_ID: MkU3OEVBNTcwNTJENDM2Qk

  # Schema Registry (validation Avro)
  schema-registry:
    image: confluentinc/cp-schema-registry:7.8.3
    ports:
      - "8081:8081"
    environment:
      SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS: kafka:9092
      SCHEMA_REGISTRY_HOST_NAME: schema-registry

  # PostgreSQL (bases de données)
  postgres:
    image: postgres:16
    ports:
      - "5432:5432"
    environment:
      POSTGRES_USER: admin
      POSTGRES_PASSWORD: admin
      POSTGRES_MULTIPLE_DATABASES: platform_db,publisher_db
    volumes:
      - ./init-multi-db.sql:/docker-entrypoint-initdb.d/init.sql

  # Kafka UI (interface web)
  kafka-ui:
    image: provectuslabs/kafka-ui:latest
    ports:
      - "8080:8080"
    environment:
      KAFKA_CLUSTERS_0_NAME: local
      KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka:9092
      KAFKA_CLUSTERS_0_SCHEMAREGISTRY: http://schema-registry:8081

  # PgAdmin (interface PostgreSQL)
  pgadmin:
    image: dpage/pgadmin4:latest
    ports:
      - "5050:80"
    environment:
      PGADMIN_DEFAULT_EMAIL: admin@admin.com
      PGADMIN_DEFAULT_PASSWORD: admin
```

---

### 5.2 Dépendances de démarrage

```
1. Docker Compose UP
   ├─> PostgreSQL démarre
   ├─> Kafka démarre (KRaft = sans Zookeeper)
   ├─> Schema Registry se connecte à Kafka
   ├─> Kafka UI et PgAdmin se connectent
   └─> Tous prêts en 30-60 secondes

2. Créer les topics Kafka (manuel ou auto-create)
   kafka-topics.sh --create --topic game-released ...

3. Démarrer les services Java/Kotlin
   ├─> Platform Service (port 8082)
   ├─> Publisher Service (port 8083)
   ├─> Analytics Service (pas de port - Kafka Streams)
   └─> Player Simulator (JavaFX desktop)

4. Système opérationnel ✅
```

**Ordre de démarrage:**
```
1. Docker (infrastructure)
   ↓
2. Schema Registry vérifie connexion Kafka
   ↓
3. Platform Service (REST API central)
   ↓
4. Publisher Service (REST API + UI)
   ↓
5. Analytics Service (Kafka Streams)
   ↓
6. Player Simulator (client JavaFX)
```

---

### 5.3 Health Checks

**Vérifier Kafka:**
```bash
curl http://localhost:9092
# Kafka répond sur ce port
```

**Vérifier Schema Registry:**
```bash
curl http://localhost:8081/subjects
# Retourne: ["GameReleased-value", "GameCrashReported-value", ...]
```

**Vérifier Platform Service:**
```bash
curl http://localhost:8082/api/health
# Response: {"status": "UP", "service": "platform-service", ...}
```

**Vérifier PostgreSQL:**
```bash
psql -h localhost -U admin -d platform_db
# Connexion réussie
```

---

## 6. Scalabilité et déploiement

### 6.1 Scalabilité horizontale

**Kafka Partitions:**
```
Topic: game-crash-reported
Partitions: 3

Si 1 Analytics Service instance:
  → Traite partitions 0, 1, 2

Si 3 Analytics Service instances:
  → Instance 1: partition 0
  → Instance 2: partition 1
  → Instance 3: partition 2
  → Throughput 3x plus élevé
```

**Ajout d'instances:**
```bash
# Terminal 1
cd analytics-service-kotlin
.\gradlew run

# Terminal 2 (même service, instance 2)
cd analytics-service-kotlin
.\gradlew run

→ Kafka Streams fait le rebalancing automatiquement
→ Partitions redistribuées entre les 2 instances
```

---

### 6.2 Stratégie de déploiement

**Blue-Green Deployment:**
```
Version actuelle (Blue):
  - Platform Service v1.0
  - Publisher Service v1.0

Déploiement nouvelle version (Green):
  1. Déployer Platform Service v1.1 (nouveau serveur)
  2. Tester sur Green
  3. Basculer le traffic Blue → Green
  4. Arrêter Blue si succès
```

**Rolling Deployment:**
```
3 instances de Platform Service

1. Déployer v1.1 sur instance 1
   → Instances 2 et 3 en v1.0 (service continue)

2. Déployer v1.1 sur instance 2
   → Instance 3 en v1.0

3. Déployer v1.1 sur instance 3
   → Tout en v1.1, 0 downtime
```

---

### 6.3 Monitoring et observabilité

**Métriques Kafka:**
```
- Lag des consumers (messages en retard)
- Throughput (messages/seconde)
- Taille des topics
- Partition distribution
```

**Métriques Services:**
```
- Response time API REST
- Taux d'erreurs HTTP
- Nombre de transactions/seconde
- Utilisation mémoire JVM
```

**Métriques Kafka Streams:**
```
- Records processed rate
- Process latency
- State store size
- Rebalance frequency
```

**Outils:**
- **Kafka UI** : http://localhost:8080 (topics, consumers, messages)
- **PgAdmin** : http://localhost:5050 (données PostgreSQL)
- **Prometheus + Grafana** : (à ajouter pour métriques avancées)
- **ELK Stack** : (à ajouter pour logs centralisés)

---

## 🎓 Conclusion

Le **VideoGamePlatform** est une architecture **event-driven** professionnelle avec :

✅ **Microservices** - 4 services indépendants et scalables  
✅ **Event-Driven** - Communication asynchrone via Kafka  
✅ **CQRS** - Séparation lecture/écriture  
✅ **Schema Registry** - Contrats d'interface évolutifs  
✅ **Stream Processing** - Agrégations temps réel (Kafka Streams)  
✅ **Polyglot** - Java + Kotlin, REST + Events  
✅ **Database per Service** - Autonomie des services  
✅ **Docker** - Infrastructure conteneurisée  

**Stack technique:**
- Backend: Java 23, Kotlin, Spring Boot, JavaFX
- Messaging: Kafka 7.8.3 (KRaft), Schema Registry, Avro
- Database: PostgreSQL 16
- Stream Processing: Kafka Streams
- Build: Gradle (Kotlin DSL)
- Infrastructure: Docker Compose

**Services:**
1. **Platform Service** - API REST centrale (8082)
2. **Publisher Service** - UI éditeur + API (8083)
3. **Analytics Service** - Stream processing (Kafka Streams)
4. **Player Simulator** - Client JavaFX

**Topics Kafka principaux:**
- `game-released`, `game-patch-released`, `dlc-created`
- `game-crash-reported`, `crash-aggregated`
- `game-session-started`, `game-session-ended`
- `game-reviewed`, `game-popularity-score`

**Pour plus de détails, consultez:**
- `services/platform-service-java/EXPLANATION.md`
- `services/publisher-service-java/EXPLANATION.md`
- `services/analytics-service-kotlin/EXPLANATION.md`
- `services/player-simulator-java/EXPLANATION.md`

---

## 🚀 Scripts de démarrage/arrêt

Le projet fournit deux scripts PowerShell pour faciliter le démarrage et l'arrêt de l'infrastructure complète.

### start-stack.ps1

Script de démarrage complet qui automatise toute la stack dans l'ordre correct :

```powershell
.\start-stack.ps1
```

**Étapes exécutées :**
1. **Vérification des prérequis** : Java et Docker
2. **Compilation Avro** : Compile et publie les schémas Avro dans Maven local
3. **Infrastructure Docker** : Démarre Kafka, PostgreSQL, Schema Registry, etc.
4. **Health checks** : Attend que Kafka soit prêt (retry automatique pendant 60 secondes)
5. **Démarrage des services** : Lance les 4 services dans des fenêtres PowerShell séparées
6. **Création des logs** : Fichiers logs dans `./logs/` pour chaque service

**Options disponibles :**

- **`-SkipAvro`** : Ignore la compilation des schémas Avro  
  Utile si les schémas sont déjà compilés et n'ont pas changé
  ```powershell
  .\start-stack.ps1 -SkipAvro
  ```

- **`-SkipDocker`** : Ignore le démarrage de Docker Compose  
  Utile si Docker est déjà en cours d'exécution
  ```powershell
  .\start-stack.ps1 -SkipDocker
  ```

- **`-SkipServices`** : Ignore le démarrage des services Java/Kotlin  
  Utile pour démarrer uniquement l'infrastructure Docker
  ```powershell
  .\start-stack.ps1 -SkipServices
  ```

- **`-NoLogs`** : N'enregistre pas les logs dans des fichiers  
  Les logs restent uniquement dans les fenêtres PowerShell
  ```powershell
  .\start-stack.ps1 -NoLogs
  ```

**Combinaison d'options :**
```powershell
# Démarrer uniquement Docker (sans Avro ni services)
.\start-stack.ps1 -SkipAvro -SkipServices

# Démarrer les services sans recompiler Avro
.\start-stack.ps1 -SkipAvro
```

### stop-stack.ps1

Script d'arrêt propre qui stoppe tous les composants :

```powershell
.\stop-stack.ps1
```

**Actions effectuées :**
1. **Arrêt des processus Java** : Détecte et arrête tous les processus Gradle en cours
2. **Arrêt Docker Compose** : Stoppe tous les conteneurs (Kafka, PostgreSQL, etc.)
3. **Nettoyage** : Affiche les fichiers de verrouillage Gradle restants

**Option disponible :**

- **`-KeepDocker`** : Conserve Docker en cours d'exécution  
  Arrête uniquement les services Java/Kotlin, garde l'infrastructure Docker
  ```powershell
  .\stop-stack.ps1 -KeepDocker
  ```

**Remarques importantes :**
- Les données Docker sont conservées dans les volumes (non supprimées par défaut)
- Pour supprimer aussi les volumes : `cd docker && docker compose down -v`
- Les fenêtres PowerShell des services doivent être fermées manuellement si elles restent ouvertes

**Workflow typique :**
```powershell
# Premier démarrage (compile tout)
.\start-stack.ps1

# Développement : arrêt/redémarrage rapide
.\stop-stack.ps1 -KeepDocker  # Garde Docker
.\start-stack.ps1 -SkipAvro -SkipDocker  # Relance seulement les services

# Arrêt complet en fin de journée
.\stop-stack.ps1
```
