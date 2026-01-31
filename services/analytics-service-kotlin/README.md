# Analytics Service - Kafka Streams Aggregator

Service Kotlin utilisant Kafka Streams pour agréger les événements de jeux en temps réel.

## 🎯 Objectifs

Ce service implémente deux agrégations principales :

### 1. **Agrégation de Crashs par Jeu** 🔥
- **Input Topic** : `game-crash-reported`
- **Output Topic** : `crash-aggregated`
- **Fenêtre** : Tumbling 1 minutes
- **Métriques** :
  - Nombre total de crashs
  - Nombre d'utilisateurs affectés

### 2. **Score de Popularité des Jeux** ⭐
- **Input Topics** : `game-session-started` + `game-crash-reported`
- **Output Topic** : `game-popularity-score`
- **Fenêtre** : Hopping 1 heure (avance de 15 minutes)
- **Formule** : Popularité = Nombre de sessions - (Nombre de crashs × 10)
- **Évaluation qualité** : EXCELLENT, GOOD, AVERAGE, POOR, CRITICAL

## 📊 Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    KAFKA TOPICS (Input)                      │
│                                                              │
│  game-crash-reported          game-session-started          │
│         │                              │                     │
│         │                              │                     │
│         ▼                              ▼                     │
│  ┌──────────────┐              ┌──────────────┐            │
│  │   Crash      │              │  Popularity  │            │
│  │ Aggregation  │              │    Score     │            │
│  │  Topology    │              │   Topology   │            │
│  └──────┬───────┘              └──────┬───────┘            │
│         │                              │                     │
│         ▼                              ▼                     │
│  crash-aggregated          game-popularity-score            │
│                                                              │
│                    KAFKA TOPICS (Output)                     │
└─────────────────────────────────────────────────────────────┘
```

## 🚀 Démarrage

### 1. Générer les schémas Avro
```bash
cd common/avro-schemas
.\gradlew.bat build
```

### 2. Lancer l'infrastructure Docker
```bash
cd docker
docker compose up -d
```

### 3. Démarrer le service
```bash
cd services/analytics-service-kotlin
.\gradlew.bat :app:run
```

## 📝 Schémas de Sortie

### GameCrashStats
```json
{
  "gameId": "game-123",
  "windowStart": 1738058400000,
  "windowEnd": 1738058700000,
  "crashCount": 15,
  "uniqueUsersAffected": 12,
  "platforms": ["PC", "PS5", "Xbox"],
  "mostCommonError": "NullPointerException"
}
```

### GamePopularityScore
```json
{
  "gameId": "game-123",
  "windowStart": 1738058400000,
  "windowEnd": 1738062000000,
  "activeSessionCount": 500,
  "crashCount": 3,
  "popularityScore": 97.0,
  "qualityRating": "EXCELLENT"
}
```

## 📊 Ratings Qualité

| Crash Rate | Rating    | Description                |
|------------|-----------|----------------------------|
| < 1%       | EXCELLENT | Très haute qualité         |
| < 5%       | GOOD      | Bonne qualité              |
| < 10%      | AVERAGE   | Qualité acceptable         |
| < 20%      | POOR      | Qualité médiocre           |
| > 20%      | CRITICAL  | Nécessite intervention     |

## 🔍 Monitoring

- **Kafka UI** : http://localhost:8080
- **Schema Registry** : http://localhost:8081/subjects

## 📁 Structure du Code

```
app/src/main/kotlin/org/example/
├── config/
│   └── KafkaStreamsConfig.kt      # Configuration Kafka Streams
├── model/
│   └── AggregationModels.kt       # Modèles d'état interne
├── topology/
│   ├── CrashAggregationTopology.kt    # Agrégation crashs
│   └── PopularityScoreTopology.kt     # Score popularité
├── serde/
│   └── JsonSerde.kt               # Sérialisation JSON
└── App.kt                         # Point d'entrée
```
