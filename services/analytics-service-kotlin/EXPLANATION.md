# 📊 GUIDE D'EXPLICATION DÉTAILLÉ - Analytics Service

Ce document explique en détail **comment fonctionne** l'Analytics Service, le service d'agrégation temps réel basé sur Kafka Streams.

---

## 📚 Table des Matières

1. [Vue d'ensemble de l'architecture](#1-vue-densemble-de-larchitecture)
2. [Kafka Streams expliqué](#2-kafka-streams-expliqué)
3. [Topologies détaillées](#3-topologies-détaillées)
4. [Fenêtrage (Windowing)](#4-fenêtrage-windowing)
5. [Sérialisation Avro](#5-sérialisation-avro)
6. [Performance et scalabilité](#6-performance-et-scalabilité)

---

## 1. Vue d'ensemble de l'architecture

### 1.1 Concept Kafka Streams

**Kafka Streams** est une bibliothèque de traitement de flux qui transforme Kafka en une plateforme de traitement événementiel en temps réel.

```
┌─────────────────────────────────────────────────────────────┐
│                    KAFKA TOPICS (Input)                      │
│                                                              │
│  game-crash-reported          game-reviewed                 │
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

**Différence avec un Consumer classique:**

| Consumer classique | Kafka Streams |
|-------------------|---------------|
| Lit → Traite → Écrit | Dataflow déclaratif |
| Gestion manuelle de l'état | État géré automatiquement (RocksDB) |
| Pas de fenêtrage intégré | Windowing natif |
| Scalabilité manuelle | Scalabilité automatique |

---

### 1.2 Architecture du service

```
┌─────────────────────────────────────────────────────────────┐
│                   Analytics Service (Kotlin)                 │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  App.kt (main)                                              │
│    │                                                         │
│    ├─> CrashAggregationTopology.build()                     │
│    │     - Input: game-crash-reported                       │
│    │     - Window: Tumbling 1 minute                        │
│    │     - Output: crash-aggregated                         │
│    │                                                         │
│    └─> PopularityScoreTopology.build()                      │
│          - Input: game-reviewed + crash-aggregated          │
│          - Window: Tumbling 1 minute                        │
│          - Output: game-popularity-score                    │
│                                                              │
│  KafkaStreamsConfig                                         │
│    - Bootstrap servers: localhost:9092                      │
│    - Schema registry: http://localhost:8081                 │
│    - Application ID: analytics-service-aggregator           │
│                                                              │
│  State Stores (RocksDB local)                               │
│    - Stockage de l'état des agrégations                    │
│    - Checkpoint automatique                                 │
│    - Récupération en cas de crash                          │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. Kafka Streams expliqué

### 2.1 Concept de Topology

Une **topology** est un graphe de traitement de flux (DAG - Directed Acyclic Graph).

```kotlin
val builder = StreamsBuilder()

// 1. Source (lecture d'un topic)
val stream: KStream<String, GameCrashReported> = builder.stream("game-crash-reported")

// 2. Transformations
stream
    .selectKey { _, crash -> crash.getGameId() }  // Réorganiser par gameId
    .groupByKey()                                  // Grouper par clé
    .windowedBy(TimeWindows.ofSizeAndGrace(...))  // Appliquer fenêtrage
    .count()                                       // Agréger

// 3. Sink (écriture vers un topic)
    .toStream()
    .to("crash-aggregated")
```

**Différence avec programmation impérative:**

```kotlin
// ❌ Programmation impérative (Consumer classique)
while (true) {
    val records = consumer.poll(Duration.ofMillis(100))
    for (record in records) {
        val gameId = record.value().getGameId()
        crashCounts[gameId] = (crashCounts[gameId] ?: 0) + 1
        // Problème: gestion manuelle de l'état, pas de fenêtrage
    }
}

// ✅ Programmation déclarative (Kafka Streams)
stream
    .selectKey { _, crash -> crash.getGameId() }
    .groupByKey()
    .windowedBy(TimeWindows.ofSizeAndGrace(Duration.ofMinutes(1), Duration.ofSeconds(1)))
    .count()
```

---

### 2.2 KStream vs KTable

```kotlin
// KStream = flux d'événements (append-only log)
val crashStream: KStream<String, GameCrashReported> = 
    builder.stream("game-crash-reported")
// Chaque crash est un nouvel événement

// KTable = état agrégé (table avec clé unique)
val crashCounts: KTable<Windowed<String>, Long> = 
    crashStream
        .groupByKey()
        .windowedBy(TimeWindows.ofSizeAndGrace(...))
        .count()
// Nombre total de crashs par gameId (mis à jour)
```

**Analogie base de données:**
- **KStream** = `INSERT INTO logs (id, data) VALUES (...)`
- **KTable** = `UPDATE stats SET count = count + 1 WHERE key = 'game-123'`

---

## 3. Topologies détaillées

### 3.1 CrashAggregationTopology

**Objectif:** Compter les crashs par jeu dans des fenêtres de 1 minute.

```kotlin
class CrashAggregationTopology {
    
    fun build(builder: StreamsBuilder) {
        val crashSerde: SpecificAvroSerde<GameCrashReported> = 
            KafkaStreamsConfig.createAvroSerde()
        
        // 1. LECTURE DU TOPIC
        val crashStream: KStream<String, GameCrashReported> = builder.stream(
            "game-crash-reported",
            Consumed.with(Serdes.String(), crashSerde)
                .withTimestampExtractor { record, _ -> record.timestamp() }
        )
        
        // 2. AGRÉGATION PAR FENÊTRE
        val crashCounts: KTable<Windowed<String>, Long> = crashStream
            .selectKey { _, crash -> crash.getGameId().toString() }
            .groupByKey(Grouped.with(Serdes.String(), crashSerde))
            .windowedBy(TimeWindows.ofSizeAndGrace(
                Duration.ofMinutes(1),  // Fenêtre de 1 minute
                Duration.ofSeconds(1)   // Grace period
            ))
            .count()  // Compte les événements
        
        // 3. CONVERSION EN STREAM + MAPPING
        val crashAggregation: KStream<String, CrashAggregationModel> = crashCounts
            .toStream()
            .map { windowedKey, count ->
                val aggregation = CrashAggregationModel.newBuilder()
                    .setId("${windowedKey.key()}-${windowedKey.window().start()}")
                    .setGameId(windowedKey.key())
                    .setCrashCount(count)
                    .setTimestamp(System.currentTimeMillis())
                    .setWindowStart(windowedKey.window().start())
                    .setWindowEnd(windowedKey.window().end())
                    .build()
                
                KeyValue(windowedKey.key(), aggregation)
            }
        
        // 4. ÉCRITURE VERS LE TOPIC DE SORTIE
        crashAggregation.to(
            "crash-aggregated",
            Produced.with(Serdes.String(), crashAggregatedSerde)
        )
    }
}
```

**Flux détaillé:**

```
t=0:00  Crash1 (game-123) arrive
          ↓ count=1, fenêtre [0:00-1:00]

t=0:30  Crash2 (game-123) arrive
          ↓ count=2, fenêtre [0:00-1:00]

t=1:05  Fenêtre [0:00-1:00] se ferme (grace period écoulé)
          ↓ Émission de CrashAggregationModel(gameId=game-123, count=2)
          ↓ Envoi vers topic "crash-aggregated"

t=1:10  Crash3 (game-123) arrive
          ↓ count=1, NOUVELLE fenêtre [1:00-2:00]
```

---

### 3.2 PopularityScoreTopology

**Objectif:** Calculer un score de popularité basé sur les avis et les crashs.

**Formule:**
```
Popularité = (Note moyenne × Nombre d'avis) - (Nombre de crashs × 10)
```

```kotlin
class PopularityScoreTopology {
    
    fun build(builder: StreamsBuilder) {
        // 1. AGRÉGATION DES AVIS
        val reviewStats: KTable<Windowed<String>, ReviewStats> = builder
            .stream<String, GameReviewed>("game-reviewed")
            .selectKey { _, review -> review.getGameId() }
            .groupByKey()
            .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofSeconds(60)))
            .aggregate(
                { ReviewStats(0L, 0.0) },  // Initializer
                { _, review, stats ->       // Aggregator
                    ReviewStats(
                        count = stats.count + 1,
                        totalRating = stats.totalRating + review.getRating()
                    )
                }
            )
        
        // 2. LECTURE DES CRASHS AGRÉGÉS
        val crashStream: KStream<String, CrashAggregationModel> = builder
            .stream("crash-aggregated")
        
        // 3. JOIN DES DEUX STREAMS
        val popularityScore: KStream<String, GamePopularityScore> = reviewStats
            .toStream()
            .leftJoin(
                crashStream.toTable(),
                { review, crash ->
                    val avgRating = review.totalRating / review.count
                    val crashPenalty = (crash?.getCrashCount() ?: 0) * 10
                    val score = (avgRating * review.count) - crashPenalty
                    
                    GamePopularityScore.newBuilder()
                        .setGameId(gameId)
                        .setReviewCount(review.count)
                        .setAverageRating(avgRating)
                        .setCrashCount(crash?.getCrashCount() ?: 0)
                        .setPopularityScore(score)
                        .setQualityRating(determineQuality(score))
                        .build()
                }
            )
        
        // 4. ENVOI VERS LE TOPIC DE SORTIE
        popularityScore.to("game-popularity-score")
    }
}
```

---

## 4. Fenêtrage (Windowing)

### 4.1 Types de fenêtres

**Tumbling Window (fenêtre basculante):**
```
Taille: 1 minute
Pas de chevauchement

[0:00 ─────── 1:00] [1:00 ─────── 2:00] [2:00 ─────── 3:00]
  │                │                │                │
  Event1          Event2           Event3           Event4
```

**Hopping Window (fenêtre glissante):**
```
Taille: 1 minute
Avance: 30 secondes (chevauchement)

[0:00 ─────── 1:00]
       [0:30 ─────── 1:30]
              [1:00 ─────── 2:00]
```

**Session Window (fenêtre de session):**
```
Gap d'inactivité: 5 minutes

Event1 ─── Event2 ─── [5min gap] ─── Event3 ─── Event4
└──────────────────┘                 └──────────────┘
    Session 1                           Session 2
```

---

### 4.2 Grace Period

**Problème:** Les événements peuvent arriver en retard (network latency, clock skew).

```
Fenêtre [0:00-1:00], Grace Period = 10 secondes

t=1:05  Event avec timestamp 0:55 arrive → ACCEPTÉ (dans grace period)
t=1:12  Event avec timestamp 0:58 arrive → REJETÉ (hors grace period)
```

**Configuration:**
```kotlin
TimeWindows.ofSizeAndGrace(
    Duration.ofMinutes(1),  // Taille de la fenêtre
    Duration.ofSeconds(10)  // Grace period
)
```

**Trade-off:**
- Grace period court → Résultats rapides, mais perte d'événements retardés
- Grace period long → Plus de données, mais latence accrue

---

## 5. Sérialisation Avro

### 5.1 Pourquoi Avro avec Kafka Streams ?

**Avantages:**
1. **Schéma versioning** : Évolution sans breaking changes
2. **Compacité** : Format binaire (50% plus petit que JSON)
3. **Validation** : Schema Registry valide automatiquement
4. **Compatibilité** : Forward/Backward compatibility

---

### 5.2 Configuration Avro Serde

```kotlin
object KafkaStreamsConfig {
    
    fun <T : SpecificRecord> createAvroSerde(): SpecificAvroSerde<T> {
        val serde = SpecificAvroSerde<T>()
        
        val config = mapOf(
            AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG 
                to "http://localhost:8081"
        )
        
        serde.configure(config, false) // false = value serde
        return serde
    }
}
```

**Utilisation:**
```kotlin
val crashSerde: SpecificAvroSerde<GameCrashReported> = 
    KafkaStreamsConfig.createAvroSerde()

val stream = builder.stream(
    "game-crash-reported",
    Consumed.with(Serdes.String(), crashSerde)
)
```

---

## 6. Performance et scalabilité

### 6.1 State Stores (RocksDB)

Kafka Streams stocke l'état des agrégations localement dans **RocksDB**.

```
kafka-streams-state/
└── analytics-service-aggregator/
    ├── crash-aggregation-state/
    │   └── RocksDB files (clé-valeur)
    └── popularity-score-state/
        └── RocksDB files
```

**Avantages:**
- **Performance** : Accès local ultra-rapide (pas de réseau)
- **Fault tolerance** : Changelog topic Kafka backup l'état
- **Scalabilité** : Chaque instance a son propre state store

---

### 6.2 Partitioning et Parallelism

```
Topic game-crash-reported (3 partitions)

Instance 1 → Partition 0 → State Store 1
Instance 2 → Partition 1 → State Store 2
Instance 3 → Partition 2 → State Store 3

Throughput: 3x processing parallèle
```

**Exemple:**
```
Partition 0: Crashs des jeux A, D, G
Partition 1: Crashs des jeux B, E, H
Partition 2: Crashs des jeux C, F, I

→ Chaque instance traite indépendamment sa partition
```

---

### 6.3 Exactly-Once Semantics

Kafka Streams garantit **exactly-once processing** (EOS).

**Configuration:**
```kotlin
props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, "exactly_once_v2")
```

**Comment ça marche ?**
1. **Transactions Kafka** : Lecture + Traitement + Écriture = transaction atomique
2. **Offset commit** : Commit uniquement si toute la transaction réussit
3. **Idempotence** : Si retry, pas de doublon grâce aux sequence numbers

**Exemple:**
```
1. Lit Event1 (offset=100)
2. Agrège dans state store
3. Écrit résultat dans output topic
4. Commit offset=101

Si crash avant commit:
→ Kafka Streams rejouera offset=100
→ Idempotence = même résultat (pas de doublon)
```

---

### 6.4 Monitoring

**Métriques clés:**
```kotlin
// Nombre de records traités
stream-metrics:commit-rate
stream-metrics:poll-rate

// Latence de traitement
stream-metrics:process-latency-avg
stream-metrics:process-latency-max

// State store
stream-state-metrics:put-rate
stream-state-metrics:get-rate
```

**Logging:**
```
✅ Crash Aggregation Topology built
   📥 Input: game-crash-reported
   📤 Output: crash-aggregated
   ⏱️  Window: 60 seconds (Tumbling)

🔴 Crash received: gameId=game-123
📊 Crash Aggregation produced: gameId=game-123, count=5
   Window: [2026-01-31T10:00:00 - 2026-01-31T10:01:00]
```

---

## 🎓 Conclusion

L'Analytics Service démontre la puissance de **Kafka Streams** pour :

✅ **Traitement temps réel** - Latence < 1 seconde  
✅ **Agrégations complexes** - Windowing, joins, aggregations  
✅ **Scalabilité linéaire** - Ajout d'instances = throughput augmente  
✅ **Fault tolerance** - State stores répliqués + changelog topics  
✅ **Exactly-once** - Garantie de cohérence stricte  
✅ **Déclaratif** - Code concis et maintenable (Kotlin)  

**Topologies implémentées:**
1. **CrashAggregationTopology** : Compte crashs par jeu (fenêtres 1 min)
2. **PopularityScoreTopology** : Score = (avis × note) - (crashs × 10)

**Applications réelles:**
- Alertes en temps réel (crashs > seuil)
- Tableaux de bord analytics live
- Recommandations basées sur popularité
- Monitoring qualité jeux
