# 📍 LOCALISATION DES CONFIGURATIONS - Publisher Service

## 🗄️ Configuration de la Base de Données

### ⚙️ Architecture : **Base de données dans Docker**

Le projet utilise **deux configurations possibles** :

#### 🐳 **Option 1 : PostgreSQL (Production - Docker)**
Pour production, utilisez PostgreSQL qui tourne dans Docker.

**Configuration Docker :** `docker/docker-compose.yml`
```yaml
postgres:
  image: postgres:16
  container_name: postgres
  environment:
    POSTGRES_DB: videogames_db
    POSTGRES_USER: videogames_user
    POSTGRES_PASSWORD: secretpassword
  ports:
    - "5432:5432"
```

**Configuration application.properties (à activer) :**
```properties
# PostgreSQL dans Docker
spring.datasource.url=jdbc:postgresql://localhost:5432/videogames_db
spring.datasource.username=videogames_user
spring.datasource.password=secretpassword
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

**Accès PgAdmin :** http://localhost:5050
- Email: `admin@local.com`
- Password: `admin`

#### 💾 **Option 2 : H2 (Développement - Actuellement active)**
Pour développement local rapide sans Docker.

**Configuration actuelle :** `app/src/main/resources/application.properties`
```properties
# H2 en mode fichier (développement local)
spring.datasource.url=jdbc:h2:file:./data/publisher-db;AUTO_SERVER=TRUE
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# Console H2
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

**Console H2 :** http://localhost:8082/h2-console

### 📝 Comparaison des options :

| Critère | H2 (actuel) | PostgreSQL (Docker) |
|---------|-------------|---------------------|
| **Démarrage** | Automatique avec l'app | Nécessite `docker-compose up` |
| **Performance** | Rapide pour dev | Production-ready |
| **Persistance** | Fichier local `./data/` | Volume Docker persistant |
| **Interface Web** | H2 Console (8082) | PgAdmin (5050) |
| **Environnement** | Développement | Production |

### 🎯 Configuration Auto (Spring Boot)
Spring Boot configure **automatiquement** :
- Le DataSource (via `spring.datasource.*`)
- L'EntityManagerFactory (via `spring.jpa.*`)
- Les repositories JPA (via `@EnableJpaRepositories` automatique)
- Les transactions (via `@Transactional`)

**Aucune classe de configuration manuelle n'est nécessaire !** Spring Boot détecte automatiquement le driver selon l'URL JDBC.

**Dépendances dans `build.gradle.kts` :**
```kotlin
implementation("org.springframework.boot:spring-boot-starter-data-jpa")
runtimeOnly("org.postgresql:postgresql:42.7.1")  // Pour PostgreSQL
runtimeOnly("com.h2database:h2:2.2.224")        // Pour H2
```

---

## 📨 Configuration Kafka

### 1️⃣ Fichier Principal : `application.properties`
**Chemin :** `app/src/main/resources/application.properties`

```properties
# ===== Configuration Kafka =====
# Bootstrap servers (depuis docker-compose)
kafka.bootstrap.servers=localhost:9092

# Schema Registry (depuis docker-compose)
kafka.schema.registry.url=http://localhost:8081

# Topics Kafka (Production)
kafka.topic.game-patched=game-patched
kafka.topic.game-metadata-updated=game-metadata-updated

# Topics Kafka (Consommation)
kafka.topic.game-crash-reported=game-crash-reported
kafka.topic.game-rating-aggregated=game-rating-aggregated

# Consumer Group
kafka.consumer.group-id=publisher-service-group
```

### 2️⃣ Classe de Configuration : `KafkaConfig.java`
**Chemin :** `app/src/main/java/com/gaming/publisher/config/KafkaConfig.java`

Cette classe configure :

#### 📤 **Producteurs Kafka** (pour PUBLIER des événements)
```java
@Bean
@Primary
public Map<String, Object> producerConfigs() {
    Map<String, Object> props = new HashMap<>();
    
    // Serveur Kafka
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers); // localhost:9092
    
    // Sérialisation
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
    
    // Schema Registry
    props.put("schema.registry.url", schemaRegistryUrl); // http://localhost:8081
    
    // Fiabilité
    props.put(ProducerConfig.ACKS_CONFIG, "all"); // Attendre confirmation de tous les réplicas
    props.put(ProducerConfig.RETRIES_CONFIG, 3); // Retry 3 fois en cas d'erreur
    props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true); // Éviter les doublons
    
    return props;
}
```

#### 📥 **Consommateurs Kafka** (pour RECEVOIR des événements)
```java
@Bean
public Map<String, Object> consumerConfigs() {
    Map<String, Object> props = new HashMap<>();
    
    // Serveur Kafka
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    
    // Groupe de consommateurs (pour load balancing)
    props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId); // publisher-service-group
    
    // Désérialisation
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
    
    // Schema Registry
    props.put("schema.registry.url", schemaRegistryUrl);
    
    // Utiliser classes Avro générées (pas GenericRecord)
    props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
    
    // Commit automatique des offsets
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
    props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 1000);
    
    // Lire depuis le début si aucun offset trouvé
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    
    return props;
}
```

### 3️⃣ Classes qui utilisent Kafka :

#### 📤 **Producers** (Publient des événements)
- **`GamePatchedProducer`** → Topic : `game-patched`
  - Injecte directement : `kafka.bootstrap.servers`, `kafka.schema.registry.url`, `kafka.topic.game-patched`
  
- **`GameMetadataProducer`** → Topic : `game-metadata-updated`
  - Injecte directement : `kafka.bootstrap.servers`, `kafka.schema.registry.url`, `kafka.topic.game-metadata-updated`

#### 📥 **Consumers** (Reçoivent des événements)
- **`GameCrashConsumer`** → Topic : `game-crash-reported`
  - Utilise le bean `crashConsumerConfigs()`
  
- **`GameRatingConsumer`** → Topic : `game-rating-aggregated`
  - Utilise le bean `ratingConsumerConfigs()`

---

## 🐳 Configuration Docker (Infrastructure Complète)

### Fichier : `docker/docker-compose.yml`

Ce fichier démarre **TOUTE l'infrastructure** nécessaire pour le projet :

```yaml
services:
  # 📨 Broker Kafka (serveur de messages)
  kafka:
    image: confluentinc/cp-kafka:7.8.3
    ports:
      - "9092:9092"  # ← Port exposé pour l'application
    environment:
      # KRaft mode (pas besoin de Zookeeper)
      KAFKA_NODE_ID: 1
      KAFKA_PROCESS_ROLES: "broker,controller"
      # Listeners pour accès interne Docker + externe localhost
      KAFKA_ADVERTISED_LISTENERS: "PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092"

  # 📋 Schema Registry (validation des schémas Avro)
  schema-registry:
    image: confluentinc/cp-schema-registry:7.8.3
    ports:
      - "8081:8081"  # ← Port exposé pour l'application
    environment:
      SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS: "PLAINTEXT://kafka:29092"
    depends_on:
      - kafka

  # 🎨 Interface Web Kafka (visualisation topics, messages, schemas)
  kafka-ui:
    image: provectuslabs/kafka-ui:latest
    ports:
      - "8080:8080"  # ← Interface web : http://localhost:8080
    environment:
      KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: "kafka:29092"
      KAFKA_CLUSTERS_0_SCHEMAREGISTRY: "http://schema-registry:8081"

  # 🗄️ PostgreSQL (base de données production)
  postgres:
    image: postgres:16
    ports:
      - "5432:5432"  # ← Port exposé pour l'application
    environment:
      POSTGRES_DB: videogames_db
      POSTGRES_USER: videogames_user
      POSTGRES_PASSWORD: secretpassword
    volumes:
      - pgdata:/var/lib/postgresql/data  # Persistance des données

  # 🎛️ PgAdmin (interface web pour gérer PostgreSQL)
  pgadmin:
    image: dpage/pgadmin4:latest
    ports:
      - "5050:80"  # ← Interface web : http://localhost:5050
    environment:
      PGADMIN_DEFAULT_EMAIL: admin@local.com
      PGADMIN_DEFAULT_PASSWORD: admin
    depends_on:
      - postgres

volumes:
  pgdata:  # Volume Docker pour persistance PostgreSQL

networks:
  app-net:  # Réseau partagé entre tous les conteneurs
```

### 🚀 Commandes Docker

#### Démarrer toute l'infrastructure
```bash
cd docker
docker-compose up -d
```

#### Vérifier que tout fonctionne
```bash
docker-compose ps
```

#### Voir les logs
```bash
docker-compose logs -f kafka        # Logs Kafka
docker-compose logs -f postgres     # Logs PostgreSQL
docker-compose logs -f schema-registry  # Logs Schema Registry
```

#### Arrêter l'infrastructure
```bash
docker-compose down
```

#### Arrêter ET supprimer les données
```bash
docker-compose down -v  # ⚠️ Supprime les volumes (données perdues)
```

### 🌐 URLs des services Docker

| Service | URL | Credentials |
|---------|-----|-------------|
| **Kafka Broker** | `localhost:9092` | - |
| **Schema Registry** | `http://localhost:8081` | - |
| **Kafka UI** | `http://localhost:8080` | - |
| **PostgreSQL** | `localhost:5432` | user: `videogames_user` / pass: `secretpassword` |
| **PgAdmin** | `http://localhost:5050` | email: `admin@local.com` / pass: `admin` |

### 📊 Architecture Docker

```
┌─────────────────────────────────────────────────────┐
│            DOCKER HOST (Votre machine)               │
│                                                      │
│  ┌──────────────────────────────────────────────┐  │
│  │  Container: kafka (Port 9092)                │  │
│  │  - Stocke les messages                       │  │
│  │  - KRaft mode (sans Zookeeper)               │  │
│  └──────────────────────────────────────────────┘  │
│                      ▲                              │
│                      │                              │
│  ┌──────────────────────────────────────────────┐  │
│  │  Container: schema-registry (Port 8081)      │  │
│  │  - Valide les schémas Avro                   │  │
│  │  - Gère les versions de schémas              │  │
│  └──────────────────────────────────────────────┘  │
│                                                      │
│  ┌──────────────────────────────────────────────┐  │
│  │  Container: postgres (Port 5432)             │  │
│  │  - Base de données production                │  │
│  │  - Volume: pgdata (persistant)               │  │
│  └──────────────────────────────────────────────┘  │
│                                                      │
│  ┌──────────────────────────────────────────────┐  │
│  │  Container: kafka-ui (Port 8080)             │  │
│  │  - Interface web Kafka                       │  │
│  └──────────────────────────────────────────────┘  │
│                                                      │
│  ┌──────────────────────────────────────────────┐  │
│  │  Container: pgadmin (Port 5050)              │  │
│  │  - Interface web PostgreSQL                  │  │
│  └──────────────────────────────────────────────┘  │
│                                                      │
└─────────────────────────────────────────────────────┘
                      ▲
                      │ Connexion depuis localhost
                      │
┌─────────────────────────────────────────────────────┐
│     APPLICATION SPRING BOOT (Port 8082)             │
│     - Se connecte à Kafka (localhost:9092)          │
│     - Se connecte à PostgreSQL (localhost:5432)     │
│       OU H2 (mode développement)                    │
└─────────────────────────────────────────────────────┘
```

---

## ⚠️ Note Importante : Kafka Streams

**Votre projet N'utilise PAS Kafka Streams** actuellement. Il utilise :
- **Kafka Producer API** : Pour publier des événements
- **Kafka Consumer API** : Pour consommer des événements

**Kafka Streams** est une bibliothèque pour le traitement de flux en temps réel (transformations, agrégations, jointures). Si vous souhaitez l'ajouter, vous devrez :
1. Ajouter la dépendance `kafka-streams` dans `build.gradle.kts`
2. Créer une classe `@Configuration` avec des `StreamsBuilder`

---

## 📊 Résumé Visuel

```
┌─────────────────────────────────────────────────────┐
│            APPLICATION PROPERTIES                    │
│  • spring.datasource.* → Base H2                    │
│  • kafka.bootstrap.servers → localhost:9092         │
│  • kafka.schema.registry.url → localhost:8081      │
└─────────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────────┐
│             KAFKACONFIG.JAVA                        │
│  • producerConfigs() → Configuration des producers  │
│  • consumerConfigs() → Configuration des consumers  │
└─────────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────────┐
│         PRODUCERS & CONSUMERS                       │
│  • GamePatchedProducer → Publie patches            │
│  • GameMetadataProducer → Publie métadonnées       │
│  • GameCrashConsumer → Reçoit crashs               │
│  • GameRatingConsumer → Reçoit ratings             │
└─────────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────────┐
│         INFRASTRUCTURE (Docker)                     │
│  • Kafka Broker → Port 9092                        │
│  • Schema Registry → Port 8081                     │
│  • Kafka UI → Port 8080                            │
└─────────────────────────────────────────────────────┘
```

---

## ✅ Conclusion

| Composant | Fichier de Configuration | Emplacement | Environnement |
|-----------|-------------------------|-------------|---------------|
| **Base de données H2** | `application.properties` | `app/src/main/resources/` | 💻 Développement local |
| **Base de données PostgreSQL** | `docker-compose.yml` | `docker/` | 🐳 Docker (Production) |
| **Kafka (connexion)** | `application.properties` | `app/src/main/resources/` | 💻 Application |
| **Kafka (configs détaillées)** | `KafkaConfig.java` | `app/src/main/java/com/gaming/publisher/config/` | 💻 Application |
| **Kafka (infrastructure)** | `docker-compose.yml` | `docker/` | 🐳 Docker |
| **Schema Registry** | `docker-compose.yml` | `docker/` | 🐳 Docker |
| **Kafka UI** | `docker-compose.yml` | `docker/` | 🐳 Docker |
| **PgAdmin** | `docker-compose.yml` | `docker/` | 🐳 Docker |
| **Kafka Streams** | ❌ Non utilisé | - | - |

### 🎯 Récapitulatif de l'architecture

```
┌─────────────────────────────────────────────────────┐
│  DOCKER (Infrastructure)                             │
│  ├─ Kafka Broker (9092)                             │
│  ├─ Schema Registry (8081)                          │
│  ├─ Kafka UI (8080)                                 │
│  ├─ PostgreSQL (5432)                               │
│  └─ PgAdmin (5050)                                  │
└─────────────────────────────────────────────────────┘
                      ▲
                      │ Connexion TCP
                      │
┌─────────────────────────────────────────────────────┐
│  SPRING BOOT APPLICATION (8082)                     │
│  ├─ KafkaConfig.java → Configure Kafka              │
│  ├─ application.properties → URLs & credentials     │
│  ├─ Producers → Publient sur Kafka                  │
│  ├─ Consumers → Écoutent Kafka                      │
│  └─ JPA Repositories → Accèdent à la BDD            │
└─────────────────────────────────────────────────────┘
```

### 📝 Notes importantes

1. **🐳 Docker obligatoire** : Pour utiliser Kafka et PostgreSQL, vous DEVEZ démarrer Docker :
   ```bash
   cd docker && docker-compose up -d
   ```

2. **💾 Mode développement** : Pour développement rapide sans Docker, l'application utilise H2 (actuellement configuré)

3. **🔄 Basculer vers PostgreSQL** : Modifiez `application.properties` :
   ```properties
   # Commentez la config H2
   #spring.datasource.url=jdbc:h2:file:./data/publisher-db
   
   # Décommentez la config PostgreSQL
   spring.datasource.url=jdbc:postgresql://localhost:5432/videogames_db
   spring.datasource.username=videogames_user
   spring.datasource.password=secretpassword
   ```

Tous les fichiers sont bien organisés et respectent les conventions Spring Boot ! 🎯

