# 🐳 Docker Infrastructure - VideoGamePlatform

## 📋 Vue d'ensemble

Ce dossier contient la configuration Docker Compose pour toute l'infrastructure nécessaire à la plateforme VideoGamePlatform.

---

## 🚀 Démarrage Rapide

```bash
# Démarrer toute l'infrastructure
docker-compose up -d

# Vérifier que tout fonctionne
docker-compose ps

# Arrêter l'infrastructure
docker-compose down
```

---

## 📦 Services Inclus

| Service | Image | Port | Description |
|---------|-------|------|-------------|
| **Kafka** | `confluentinc/cp-kafka:7.8.3` | 9092 | Bus de messages (architecture événementielle) |
| **Schema Registry** | `confluentinc/cp-schema-registry:7.8.3` | 8081 | Validation des schémas Avro |
| **Kafka UI** | `provectuslabs/kafka-ui:latest` | 8080 | Interface web pour Kafka |
| **PostgreSQL** | `postgres:16` | 5432 | Base de données relationnelle |
| **PgAdmin** | `dpage/pgadmin4:latest` | 5050 | Interface web pour PostgreSQL |

---

## 🌐 URLs d'Accès

### Interfaces Web

- **Kafka UI** : http://localhost:8080
  - Visualiser les topics, messages, schémas
  - Gérer les consumer groups
  - Pas d'authentification requise

- **PgAdmin** : http://localhost:5050
  - Email : `admin@local.com`
  - Mot de passe : `admin`
  
### Connexions Programmatiques

- **Kafka Bootstrap Servers** : `localhost:9092`
- **Schema Registry** : `http://localhost:8081`
- **PostgreSQL** : `localhost:5432`
  - Database : `videogames_db`
  - Username : `videogames_user`
  - Password : `secretpassword`

---

## 🔧 Configuration dans les Applications

### application.properties (Spring Boot)

```properties
# Kafka
kafka.bootstrap.servers=localhost:9092
kafka.schema.registry.url=http://localhost:8081

# PostgreSQL (production)
spring.datasource.url=jdbc:postgresql://localhost:5432/videogames_db
spring.datasource.username=videogames_user
spring.datasource.password=secretpassword
```

---

## 📚 Commandes Utiles

### Gestion des Services

```bash
# Démarrer
docker-compose up -d

# Arrêter (conserver les données)
docker-compose stop

# Redémarrer
docker-compose restart

# Arrêter et supprimer les conteneurs (données conservées)
docker-compose down

# Supprimer TOUT (⚠️ données perdues)
docker-compose down -v
```

### Logs

```bash
# Tous les services
docker-compose logs -f

# Un service spécifique
docker-compose logs -f kafka
docker-compose logs -f postgres
docker-compose logs -f schema-registry
```

### Vérification

```bash
# État des conteneurs
docker-compose ps

# Ressources utilisées
docker stats

# Volumes
docker volume ls
docker volume inspect docker_pgdata
```

---

## 🔍 Tests de Connectivité

### Kafka

```bash
# Lister les topics
docker exec -it kafka kafka-topics --bootstrap-server localhost:9092 --list

# Créer un topic de test
docker exec -it kafka kafka-topics --bootstrap-server localhost:9092 \
  --create --topic test --partitions 3 --replication-factor 1

# Produire un message
echo "Hello Kafka" | docker exec -i kafka kafka-console-producer \
  --bootstrap-server localhost:9092 --topic test

# Consommer les messages
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 --topic test --from-beginning
```

### Schema Registry

```bash
# Lister les schémas
curl http://localhost:8081/subjects

# Obtenir les types de schémas supportés
curl http://localhost:8081/schemas/types

# Configuration
curl http://localhost:8081/config
```

### PostgreSQL

```bash
# Connexion psql
docker exec -it postgres psql -U videogames_user -d videogames_db

# Dans psql :
\dt                      # Lister les tables
\d+ games               # Structure de la table games
SELECT COUNT(*) FROM games;
\q                      # Quitter
```

---

## 🛠️ Configuration Avancée

### Modifier le docker-compose.yml

#### Changer un port

```yaml
services:
  kafka-ui:
    ports:
      - "9090:8080"  # Mapper le port 9090 au lieu de 8080
```

#### Augmenter la mémoire Kafka

```yaml
services:
  kafka:
    environment:
      KAFKA_HEAP_OPTS: "-Xmx2G -Xms1G"
```

#### Ajouter des variables d'environnement PostgreSQL

```yaml
services:
  postgres:
    environment:
      POSTGRES_DB: videogames_db
      POSTGRES_USER: videogames_user
      POSTGRES_PASSWORD: secretpassword
      POSTGRES_INITDB_ARGS: "--encoding=UTF-8"
```

---

## 🐛 Troubleshooting

### Problème : Port déjà utilisé

**Erreur :**
```
Error: bind: address already in use
```

**Solution :**
```bash
# Windows - Trouver le processus
netstat -ano | findstr "9092"
taskkill /PID <PID> /F

# Linux/Mac
lsof -i :9092
kill -9 <PID>

# Ou changer le port dans docker-compose.yml
```

### Problème : Kafka ne démarre pas

**Erreur :**
```
Cluster ID mismatch
```

**Solution :**
```bash
# Supprimer les données et redémarrer
docker-compose down -v
docker-compose up -d
```

### Problème : PostgreSQL n'accepte pas les connexions

**Solution :**
```bash
# Vérifier les logs
docker-compose logs postgres

# Redémarrer
docker-compose restart postgres

# Vérifier la connexion
docker exec -it postgres psql -U videogames_user -d videogames_db
```

### Problème : Schema Registry ne trouve pas Kafka

**Solution :**
```bash
# Vérifier que Kafka est démarré
docker-compose ps kafka

# Vérifier la variable d'environnement
docker exec schema-registry env | grep BOOTSTRAP

# Redémarrer dans le bon ordre
docker-compose restart kafka
docker-compose restart schema-registry
```

---

## 📊 Monitoring

### Ressources Système

```bash
# CPU et mémoire en temps réel
docker stats

# Espace disque
docker system df

# Volumes
docker volume ls
```

### Kafka Topics

Via Kafka UI : http://localhost:8080

Ou en ligne de commande :
```bash
docker exec -it kafka kafka-topics --bootstrap-server localhost:9092 --list
docker exec -it kafka kafka-topics --bootstrap-server localhost:9092 \
  --describe --topic game-patched
```

### PostgreSQL Tables

Via PgAdmin : http://localhost:5050

Ou en ligne de commande :
```bash
docker exec -it postgres psql -U videogames_user -d videogames_db -c "\dt"
```

---

## 🔐 Sécurité

### Changer les Mots de Passe

**PostgreSQL :**
```yaml
services:
  postgres:
    environment:
      POSTGRES_PASSWORD: votre_mot_de_passe_securise
```

**PgAdmin :**
```yaml
services:
  pgadmin:
    environment:
      PGADMIN_DEFAULT_PASSWORD: votre_mot_de_passe_securise
```

### Utiliser des Secrets Docker

Pour production, utilisez Docker secrets :
```yaml
services:
  postgres:
    secrets:
      - postgres_password
    environment:
      POSTGRES_PASSWORD_FILE: /run/secrets/postgres_password

secrets:
  postgres_password:
    file: ./secrets/postgres_password.txt
```

---

## 📦 Sauvegarde et Restauration

### Sauvegarder PostgreSQL

```bash
# Dump de la base
docker exec -t postgres pg_dump -U videogames_user videogames_db > backup.sql

# Dump avec compression
docker exec -t postgres pg_dump -U videogames_user videogames_db | gzip > backup.sql.gz
```

### Restaurer PostgreSQL

```bash
# Depuis un fichier
cat backup.sql | docker exec -i postgres psql -U videogames_user -d videogames_db

# Depuis un fichier compressé
gunzip -c backup.sql.gz | docker exec -i postgres psql -U videogames_user -d videogames_db
```

### Sauvegarder les Volumes

```bash
# Sauvegarder le volume pgdata
docker run --rm -v docker_pgdata:/data -v $(pwd):/backup \
  alpine tar czf /backup/pgdata-backup.tar.gz /data
```

---

## 📚 Documentation Complète

Pour plus de détails, consultez :
- **ARCHITECTURE_DOCKER.md** : Architecture complète et explications détaillées
- **docker-compose.yml** : Configuration complète des services

---

## ✅ Checklist de Vérification

Après `docker-compose up -d`, vérifiez :

- [ ] `docker-compose ps` montre tous les services "Up"
- [ ] Kafka UI accessible : http://localhost:8080
- [ ] PgAdmin accessible : http://localhost:5050
- [ ] Topics Kafka visibles dans Kafka UI
- [ ] Base de données `videogames_db` visible dans PgAdmin
- [ ] Schema Registry répond : `curl http://localhost:8081/subjects`

---

**Infrastructure prête ! Vous pouvez maintenant lancer vos services Spring Boot.** 🚀

