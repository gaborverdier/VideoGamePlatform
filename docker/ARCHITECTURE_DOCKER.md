# 🐳 ARCHITECTURE DOCKER - VideoGamePlatform

## 📋 Vue d'ensemble

Le projet **VideoGamePlatform** utilise **Docker Compose** pour orchestrer toute l'infrastructure nécessaire :
- **Kafka** : Bus de messages pour l'architecture événementielle
- **Schema Registry** : Validation et gestion des schémas Avro
- **PostgreSQL** : Base de données relationnelle pour la production
- **Kafka UI** : Interface web pour visualiser Kafka
- **PgAdmin** : Interface web pour gérer PostgreSQL

---

## 🏗️ Architecture Complète

```
┌───────────────────────────────────────────────────────────────────┐
│                        DOCKER NETWORK (app-net)                    │
│                                                                    │
│  ┌──────────────────┐      ┌──────────────────┐                  │
│  │   KAFKA          │◄─────│ SCHEMA REGISTRY  │                  │
│  │   Port: 9092     │      │   Port: 8081     │                  │
│  │   (KRaft mode)   │      │   (Avro schemas) │                  │
│  └────────┬─────────┘      └──────────────────┘                  │
│           │                                                        │
│           │                                                        │
│  ┌────────▼─────────┐      ┌──────────────────┐                  │
│  │   KAFKA UI       │      │   POSTGRES       │                  │
│  │   Port: 8080     │      │   Port: 5432     │                  │
│  │   (Web UI)       │      │   Database       │                  │
│  └──────────────────┘      └────────┬─────────┘                  │
│                                     │                             │
│                            ┌────────▼─────────┐                  │
│                            │   PGADMIN        │                  │
│                            │   Port: 5050     │                  │
│                            │   (Web UI)       │                  │
│                            └──────────────────┘                  │
│                                                                    │
└───────────────────────────────────────────────────────────────────┘
                              ▲
                              │ Connexions depuis localhost
                              │
┌─────────────────────────────┴─────────────────────────────────────┐
│                   APPLICATIONS SPRING BOOT                         │
│                                                                    │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐            │
│  │ Publisher    │  │ Platform     │  │ Player       │            │
│  │ Service      │  │ Service      │  │ Simulator    │            │
│  │ (8082)       │  │ (8083)       │  │ (8084)       │            │
│  └──────────────┘  └──────────────┘  └──────────────┘            │
│                                                                    │
│  ┌──────────────┐  ┌──────────────┐                              │
│  │ Analytics    │  │ Quality      │                              │
│  │ Service      │  │ Service      │                              │
│  │ (Kotlin)     │  │ (Kotlin)     │                              │
│  └──────────────┘  └──────────────┘                              │
└────────────────────────────────────────────────────────────────────┘
```

---

## 🐳 Services Docker

### 1. **Kafka Broker** (Confluent Platform 7.8.3)

**Rôle :** Serveur de messages pour l'architecture événementielle

**Configuration :**
```yaml
kafka:
  image: confluentinc/cp-kafka:7.8.3
  ports:
    - "9092:9092"  # Port externe (applications)
  environment:
    # Mode KRaft (Kafka sans Zookeeper)
    KAFKA_PROCESS_ROLES: "broker,controller"
    # Listeners
    KAFKA_ADVERTISED_LISTENERS: |
      PLAINTEXT://kafka:29092,        # Accès interne Docker
      PLAINTEXT_HOST://localhost:9092  # Accès externe (applications)
```

**Connexion depuis applications :**
```properties
kafka.bootstrap.servers=localhost:9092
```

**Caractéristiques :**
- ✅ Mode **KRaft** (pas besoin de Zookeeper)
- ✅ Single-broker (simplifié pour développement)
- ✅ Replication factor = 1
- ✅ Logs persistés dans le conteneur

---

### 2. **Schema Registry** (Confluent Platform 7.8.3)

**Rôle :** Gestion centralisée des schémas Avro

**Configuration :**
```yaml
schema-registry:
  image: confluentinc/cp-schema-registry:7.8.3
  ports:
    - "8081:8081"
  environment:
    SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS: "PLAINTEXT://kafka:29092"
  depends_on:
    - kafka
```

**Connexion depuis applications :**
```properties
kafka.schema.registry.url=http://localhost:8081
```

**API REST :**
```bash
# Lister tous les schémas
curl http://localhost:8081/subjects

# Obtenir un schéma
curl http://localhost:8081/subjects/game-patched-value/versions/latest
```

**Caractéristiques :**
- ✅ Validation automatique des schémas
- ✅ Gestion des versions (backward/forward compatibility)
- ✅ Stockage dans Kafka (_schemas topic)

---

### 3. **Kafka UI** (Provectus)

**Rôle :** Interface web pour visualiser et gérer Kafka

**Configuration :**
```yaml
kafka-ui:
  image: provectuslabs/kafka-ui:latest
  ports:
    - "8080:8080"
  environment:
    KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: "kafka:29092"
    KAFKA_CLUSTERS_0_SCHEMAREGISTRY: "http://schema-registry:8081"
```

**URL :** http://localhost:8080

**Fonctionnalités :**
- 📊 Visualiser les topics et leurs partitions
- 📨 Consulter les messages
- 📋 Voir les schémas Avro enregistrés
- 👥 Gérer les consumer groups
- 🔍 Rechercher dans les messages
- ⚙️ Modifier les configurations

---

### 4. **PostgreSQL 16**

**Rôle :** Base de données relationnelle production

**Configuration :**
```yaml
postgres:
  image: postgres:16
  ports:
    - "5432:5432"
  environment:
    POSTGRES_DB: videogames_db
    POSTGRES_USER: videogames_user
    POSTGRES_PASSWORD: secretpassword
  volumes:
    - pgdata:/var/lib/postgresql/data  # Volume persistant
```

**Connexion depuis applications :**
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/videogames_db
spring.datasource.username=videogames_user
spring.datasource.password=secretpassword
```

**Connexion directe (psql) :**
```bash
psql -h localhost -p 5432 -U videogames_user -d videogames_db
# Password: secretpassword
```

**Caractéristiques :**
- ✅ Version 16 (dernière stable)
- ✅ Données persistées dans volume Docker `pgdata`
- ✅ Encodage UTF-8
- ✅ Port standard 5432

---

### 5. **PgAdmin 4**

**Rôle :** Interface web pour gérer PostgreSQL

**Configuration :**
```yaml
pgadmin:
  image: dpage/pgadmin4:latest
  ports:
    - "5050:80"
  environment:
    PGADMIN_DEFAULT_EMAIL: admin@local.com
    PGADMIN_DEFAULT_PASSWORD: admin
```

**URL :** http://localhost:5050

**Identifiants :**
- Email : `admin@local.com`
- Mot de passe : `admin`

**Configuration serveur PostgreSQL dans PgAdmin :**
1. Cliquer sur "Add New Server"
2. Onglet "General" :
   - Name : `VideoGames DB`
3. Onglet "Connection" :
   - Host : `postgres` (nom du conteneur Docker)
   - Port : `5432`
   - Database : `videogames_db`
   - Username : `videogames_user`
   - Password : `secretpassword`

**Fonctionnalités :**
- 📊 Visualiser les tables et données
- 🔍 Exécuter des requêtes SQL
- 📈 Voir les statistiques
- 🔧 Gérer les utilisateurs et permissions
- 💾 Importer/Exporter des données

---

## 🚀 Commandes Docker Compose

### Démarrer toute l'infrastructure
```bash
cd docker
docker-compose up -d
```

**Ordre de démarrage automatique :**
1. Kafka (pas de dépendances)
2. Schema Registry (dépend de Kafka)
3. Kafka UI (dépend de Kafka + Schema Registry)
4. PostgreSQL (pas de dépendances)
5. PgAdmin (dépend de PostgreSQL)

### Vérifier l'état des services
```bash
docker-compose ps
```

**Sortie attendue :**
```
NAME              IMAGE                                    STATUS
kafka             confluentinc/cp-kafka:7.8.3             Up 30 seconds
schema-registry   confluentinc/cp-schema-registry:7.8.3   Up 25 seconds
kafka-ui          provectuslabs/kafka-ui:latest           Up 20 seconds
postgres          postgres:16                              Up 30 seconds
pgadmin           dpage/pgadmin4:latest                   Up 25 seconds
```

### Voir les logs en temps réel
```bash
# Tous les services
docker-compose logs -f

# Un service spécifique
docker-compose logs -f kafka
docker-compose logs -f schema-registry
docker-compose logs -f postgres
```

### Redémarrer un service
```bash
docker-compose restart kafka
docker-compose restart postgres
```

### Arrêter l'infrastructure (sans supprimer les données)
```bash
docker-compose stop
```

### Démarrer après un stop
```bash
docker-compose start
```

### Arrêter ET supprimer les conteneurs (données conservées dans volumes)
```bash
docker-compose down
```

### Arrêter ET supprimer TOUT (⚠️ DONNÉES PERDUES)
```bash
docker-compose down -v  # Supprime aussi les volumes
```

### Reconstruire les images
```bash
docker-compose build
docker-compose up -d
```

---

## 🔍 Vérification du Bon Fonctionnement

### 1. Vérifier Kafka
```bash
# Depuis le conteneur Kafka
docker exec -it kafka kafka-topics --bootstrap-server localhost:9092 --list

# Créer un topic de test
docker exec -it kafka kafka-topics --bootstrap-server localhost:9092 \
  --create --topic test --partitions 3 --replication-factor 1

# Envoyer un message de test
docker exec -it kafka kafka-console-producer --bootstrap-server localhost:9092 --topic test
> Hello Kafka!
> ^C

# Lire les messages
docker exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 \
  --topic test --from-beginning
```

### 2. Vérifier Schema Registry
```bash
# API REST
curl http://localhost:8081/subjects
curl http://localhost:8081/schemas/types
curl http://localhost:8081/config
```

### 3. Vérifier PostgreSQL
```bash
# Connexion psql
docker exec -it postgres psql -U videogames_user -d videogames_db

# Dans psql
\dt                    -- Liste des tables
\d+ games             -- Structure de la table games
SELECT COUNT(*) FROM games;
```

### 4. Vérifier les URLs
```bash
# Kafka UI
curl http://localhost:8080

# PgAdmin
curl http://localhost:5050

# Schema Registry
curl http://localhost:8081
```

---

## 📊 Ports Utilisés

| Service | Port | Protocole | Usage |
|---------|------|-----------|-------|
| **Kafka** | 9092 | TCP | Bootstrap servers (applications) |
| **Kafka** | 29092 | TCP | Listener interne Docker |
| **Kafka** | 29093 | TCP | Controller KRaft |
| **Schema Registry** | 8081 | HTTP | API REST |
| **Kafka UI** | 8080 | HTTP | Interface web |
| **PostgreSQL** | 5432 | TCP | Connexion BD |
| **PgAdmin** | 5050 | HTTP | Interface web |

**⚠️ Assurez-vous que ces ports ne sont pas déjà utilisés !**

```bash
# Windows
netstat -ano | findstr "9092"
netstat -ano | findstr "5432"
netstat -ano | findstr "8080"

# Linux/Mac
lsof -i :9092
lsof -i :5432
lsof -i :8080
```

---

## 🗂️ Volumes Docker (Persistance)

### Volume PostgreSQL
```bash
# Voir les volumes
docker volume ls

# Inspecter le volume pgdata
docker volume inspect docker_pgdata

# Sauvegarder les données
docker exec -t postgres pg_dump -U videogames_user videogames_db > backup.sql

# Restaurer les données
cat backup.sql | docker exec -i postgres psql -U videogames_user -d videogames_db
```

---

## 🔧 Troubleshooting

### Problème : Kafka ne démarre pas
```bash
# Vérifier les logs
docker-compose logs kafka

# Erreur courante : "Cluster ID mismatch"
# Solution : Supprimer les logs
docker-compose down -v
docker-compose up -d
```

### Problème : Schema Registry ne peut pas se connecter à Kafka
```bash
# Vérifier que Kafka est démarré
docker-compose ps kafka

# Vérifier les logs
docker-compose logs schema-registry

# Vérifier la variable d'environnement
docker exec schema-registry env | grep BOOTSTRAP
```

### Problème : PostgreSQL n'accepte pas les connexions
```bash
# Vérifier que le conteneur est up
docker-compose ps postgres

# Vérifier les logs
docker-compose logs postgres

# Tester la connexion
docker exec -it postgres psql -U videogames_user -d videogames_db
```

### Problème : Port déjà utilisé
```bash
# Windows - trouver le processus
netstat -ano | findstr "9092"
taskkill /PID <PID> /F

# Ou changer le port dans docker-compose.yml
ports:
  - "9093:9092"  # Mapper le port 9093 au lieu de 9092
```

---

## 🎯 Conclusion

L'infrastructure Docker fournit **tout ce qui est nécessaire** pour faire fonctionner la plateforme VideoGamePlatform :

✅ **Kafka + Schema Registry** : Architecture événementielle robuste
✅ **PostgreSQL** : Base de données production
✅ **Kafka UI + PgAdmin** : Outils de monitoring et gestion
✅ **Réseau Docker** : Communication sécurisée entre conteneurs
✅ **Volumes persistants** : Données conservées entre redémarrages

**Un seul fichier `docker-compose.yml` orchestre tout !** 🐳

