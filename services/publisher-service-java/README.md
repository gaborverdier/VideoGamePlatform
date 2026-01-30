# 🎮 Publisher Service - Video Game Platform

Service simulant le comportement d'un éditeur de jeux vidéo dans un écosystème de plateforme de gaming basé sur Kafka et Spring Boot.

## 🚀 Démarrage Rapide

### Prérequis

- Java 21+
- Docker (pour Kafka)
- Gradle 9.2+

### 1. Démarrer l'infrastructure Kafka

```bash
cd ../../docker
docker-compose up -d
```

Vérifiez que les services sont démarrés :
- Kafka: `localhost:9092`
- Schema Registry: `http://localhost:8081`
- Kafka UI: `http://localhost:8080`

### 2. Lancer le service

```bash
cd services/publisher-service-java
./gradlew bootRun
```

Le service démarre sur **http://localhost:8082**

### 3. Vérifier le fonctionnement

```bash
# Health check
curl http://localhost:8082/actuator/health

# Statistiques
curl http://localhost:8082/api/admin/stats
```

---

## 📋 Fonctionnalités

### ✅ Ce que fait le Publisher Service

1. **Gestion du catalogue de jeux**
   - Charge les jeux depuis VGSales CSV
   - Stocke en base de données H2
   - Expose une API REST pour consultation

2. **Publication de patches**
   - Déploie des mises à jour de jeux
   - Incrémente automatiquement les versions (semantic versioning)
   - Publie des événements Kafka `GamePatchedEvent`

3. **Mise à jour de métadonnées**
   - Modifie genre, plateforme, description
   - Publie des événements Kafka `GameMetadataUpdatedEvent`

4. **Analyse des crashs**
   - Consomme les événements `GameCrashReportedEvent`
   - Stocke les rapports en base
   - Alerte si seuil dépassé

5. **Suivi de la qualité**
   - Consomme les événements `GameRatingAggregatedEvent`
   - Analyse les tendances de notes
   - Identifie les jeux en difficulté

6. **Simulation automatique**
   - Génère des patches aléatoires toutes les 2 minutes
   - Utile pour démonstration et tests

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────┐
│              Publisher Service (Java)                │
├─────────────────────────────────────────────────────┤
│                                                       │
│  ┌─────────────┐        ┌──────────────┐            │
│  │   REST API  │───────>│   Services   │            │
│  │ (Controller)│        │  (Business)  │            │
│  └─────────────┘        └──────────────┘            │
│         │                       │                    │
│         │                       ▼                    │
│         │               ┌──────────────┐            │
│         └──────────────>│ Repositories │            │
│                         │     (JPA)    │            │
│                         └──────────────┘            │
│                                │                     │
│                                ▼                     │
│                         ┌──────────────┐            │
│                         │   Database   │            │
│                         │     (H2)     │            │
│                         └──────────────┘            │
│                                                       │
│  ┌──────────────┐              ┌──────────────┐     │
│  │  Producers   │──────────────>│  Consumers   │    │
│  │    Kafka     │              │    Kafka     │     │
│  └──────────────┘              └──────────────┘     │
│         │                              │             │
└─────────┼──────────────────────────────┼─────────────┘
          │                              │
          ▼                              ▼
   ┌─────────────────────────────────────────┐
   │         Apache Kafka + Schema Registry   │
   └─────────────────────────────────────────┘
```

---

## 📡 Topics Kafka

### Produits par le service

| Topic | Événement | Description |
|-------|-----------|-------------|
| `game-patched` | GamePatchedEvent | Patch déployé sur un jeu |
| `game-metadata-updated` | GameMetadataUpdatedEvent | Métadonnées modifiées |

### Consommés par le service

| Topic | Événement | Description |
|-------|-----------|-------------|
| `game-crash-reported` | GameCrashReportedEvent | Rapport de crash reçu |
| `game-rating-aggregated` | GameRatingAggregatedEvent | Statistiques de notes |

---

## 🎯 API REST

### Base URL: `http://localhost:8082/api`

### Endpoints principaux

#### Games
```bash
# Liste tous les jeux
GET /api/games

# Détails d'un jeu
GET /api/games/{id}

# Recherche
GET /api/games/search?title=zelda
```

#### Patches
```bash
# Publier un patch
POST /api/games/{id}/patch
Content-Type: application/json
{
  "changelog": "- Fixed bug\n- Improved performance"
}

# Historique des patches
GET /api/games/{id}/patches
```

#### Metadata
```bash
# Mettre à jour métadonnées
PUT /api/games/{id}/metadata
Content-Type: application/json
{
  "genre": "Action-RPG",
  "platform": "PS5",
  "description": "Epic adventure"
}
```

#### Crashes
```bash
# Liste des crashes
GET /api/crashes

# Crashes d'un jeu
GET /api/crashes/game/{id}

# Statistiques
GET /api/crashes/stats
```

#### Reviews
```bash
# Stats de notes
GET /api/reviews

# Stats d'un jeu
GET /api/reviews/game/{id}
```

#### Admin
```bash
# Recharger VGSales
POST /api/admin/reload-vgsales

# Simuler un patch
POST /api/admin/simulate-patch

# Statistiques globales
GET /api/admin/stats
```

---

## 🗄️ Base de Données

### H2 Console

- URL: http://localhost:8082/h2-console
- JDBC URL: `jdbc:h2:file:./data/publisher-db`
- Username: `sa`
- Password: *(vide)*

### Tables

- `games` - Catalogue de jeux
- `patch_history` - Historique des patches
- `crash_reports` - Rapports de crash
- `review_stats` - Statistiques de notes

---

## ⚙️ Configuration

### Fichier: `application.properties`

```properties
# Port du service
server.port=8082

# Base de données H2
spring.datasource.url=jdbc:h2:file:./data/publisher-db

# Kafka
kafka.bootstrap.servers=localhost:9092
kafka.schema.registry.url=http://localhost:8081

# Publisher
publisher.name=Activision
publisher.crash-threshold=10
publisher.vgsales.path=./data/vgsales.csv
publisher.vgsales.auto-load=true
```

### Personnalisation

**Changer l'éditeur:**
```properties
publisher.name=Electronic Arts
```

**Désactiver l'auto-load VGSales:**
```properties
publisher.vgsales.auto-load=false
```

**Ajuster le seuil d'alerte de crashs:**
```properties
publisher.crash-threshold=20
```

---

## 📊 Données VGSales

### Format CSV

Placez votre fichier `vgsales.csv` dans `./data/` :

```csv
Name,Platform,Year,Genre,Publisher,NA_Sales,EU_Sales,JP_Sales,Other_Sales,Global_Sales
Wii Sports,Wii,2006,Sports,Nintendo,41.49,29.02,3.77,8.46,82.74
Super Mario Bros.,NES,1985,Platform,Nintendo,29.08,3.58,6.81,0.77,40.24
```

Le service filtre automatiquement selon `publisher.name` configuré.

---

## 🧪 Tests

### Test manuel avec cURL

```bash
# 1. Lister les jeux
curl http://localhost:8082/api/games | jq

# 2. Publier un patch sur le premier jeu
GAME_ID=$(curl -s http://localhost:8082/api/games | jq -r '.[0].id')
curl -X POST http://localhost:8082/api/games/$GAME_ID/patch \
  -H "Content-Type: application/json" \
  -d '{"changelog": "Test patch"}'

# 3. Vérifier l'historique
curl http://localhost:8082/api/games/$GAME_ID/patches | jq

# 4. Déclencher une simulation
curl -X POST http://localhost:8082/api/admin/simulate-patch
```

### Vérifier Kafka

```bash
# Via Kafka UI
http://localhost:8080

# Ou avec kafka-console-consumer
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic game-patched \
  --from-beginning
```

---

## 📝 Logs

### Niveaux de logs

- **INFO:** Opérations normales (patches publiés, jeux chargés)
- **WARN:** Alertes (seuil de crashs dépassé, note basse)
- **ERROR:** Erreurs critiques (connexion Kafka, erreur base de données)

### Exemples

```
INFO  - ✅ Patch 1.2.3 -> 1.2.4 déployé avec succès pour 'Zelda BOTW'
WARN  - ⚠️ ALERTE PATCH URGENT ! Le jeu 'FIFA 24' a 15 crashs (seuil: 10)
ERROR - ❌ CRITIQUE ! 'Cyberpunk 2077' a une très mauvaise note de 1.8/5
```

---

## 🔧 Troubleshooting

### Le service ne démarre pas

**Erreur:** `Connection refused: localhost:9092`

**Solution:** Démarrer Kafka
```bash
cd docker
docker-compose up -d
```

### Pas de jeux en base

**Solution 1:** Vérifier le fichier CSV
```bash
ls -la ./data/vgsales.csv
```

**Solution 2:** Recharger manuellement
```bash
curl -X POST http://localhost:8082/api/admin/reload-vgsales
```

### Erreur Schema Registry

**Erreur:** `Failed to connect to http://localhost:8081`

**Solution:** Vérifier que Schema Registry est démarré
```bash
curl http://localhost:8081/subjects
docker-compose ps
```

---

## 🚀 Déploiement Production

### Build du JAR

```bash
./gradlew clean build
```

Le JAR est généré dans `app/build/libs/`

### Exécution

```bash
java -jar app/build/libs/publisher-service-1.0.0.jar \
  --spring.profiles.active=prod \
  --kafka.bootstrap.servers=kafka-prod:9092
```

### Docker (optionnel)

```dockerfile
FROM eclipse-temurin:21-jre
COPY app/build/libs/publisher-service-1.0.0.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## 📚 Documentation Complète

Pour une documentation technique détaillée, voir **[DOCUMENTATION.md](./DOCUMENTATION.md)**

---

## 🤝 Contribution

### Structure du code

- **DRY Principe:** Pas de duplication (BaseKafkaProducer, KafkaConfig centralisé)
- **SOLID:** Séparation des responsabilités (Controller/Service/Repository)
- **Commentaires:** Chaque classe/méthode est documentée

### Ajouter un nouveau topic

1. Créer le schéma Avro dans `common/avro-schemas/src/main/avro/`
2. Créer le DTO dans `dto/`
3. Créer le Producer/Consumer dans `producer/` ou `consumer/`
4. Utiliser l'injection de dépendances Spring

---

## 📞 Support

- **Issues:** Créer une issue GitHub
- **Documentation:** [DOCUMENTATION.md](./DOCUMENTATION.md)
- **Kafka UI:** http://localhost:8080
- **H2 Console:** http://localhost:8082/h2-console

---

## 📄 Licence

Ce projet est développé dans le cadre du cours d'Ingénierie des Données - Polytech 2025

---

**Bon développement ! 🚀**

