# Scripts d'envoi d'événements de test Kafka

Ce dossier contient des scripts pour envoyer des événements de test vers les topics Kafka.

## Scripts disponibles

### 1. `send-test-events.ps1` (PowerShell - Recommandé pour Windows)

**Utilisation** :
```powershell
cd scripts
.\send-test-events.ps1
```

Envoie automatiquement :
- 1 utilisateur sur `user-registered`
- 1 jeu sur `game-metadata-updated`
- 1 patch sur `game-patched`

### 2. `send_test_events.py` (Python avec Avro)

**Installation des dépendances** :
```bash
pip install confluent-kafka[avro]
```

**Utilisation** :
```bash
# Envoyer un utilisateur
python send_test_events.py --user

# Envoyer un jeu
python send_test_events.py --game

# Envoyer un patch
python send_test_events.py --patch

# Envoyer plusieurs de chaque
python send_test_events.py --all
```

### 3. `send-test-events.sh` (Bash - Linux/Mac)

**Utilisation** :
```bash
chmod +x send-test-events.sh
./send-test-events.sh
```

## Commandes manuelles

### Envoyer un utilisateur manuellement

```bash
echo "user-001:{\"userId\":\"user-001\",\"username\":\"john_doe\",\"email\":\"john@example.com\",\"registrationTimestamp\":1705598400000}" | \
docker exec -i kafka kafka-console-producer \
    --bootstrap-server localhost:9092 \
    --topic user-registered \
    --property "parse.key=true" \
    --property "key.separator=:"
```

### Envoyer un jeu manuellement

```bash
echo "game-001:{\"gameId\":\"game-001\",\"title\":\"Cyberpunk 2077\",\"publisher\":\"CD Projekt Red\",\"genre\":\"RPG\",\"platform\":\"PC\",\"description\":\"Open-world RPG\",\"eventTimestamp\":1705598400000}" | \
docker exec -i kafka kafka-console-producer \
    --bootstrap-server localhost:9092 \
    --topic game-metadata-updated \
    --property "parse.key=true" \
    --property "key.separator=:"
```

### Envoyer un patch manuellement

```bash
echo "game-001:{\"gameId\":\"game-001\",\"version\":\"1.5.2\",\"patchTimestamp\":1705598400000}" | \
docker exec -i kafka kafka-console-producer \
    --bootstrap-server localhost:9092 \
    --topic game-patched \
    --property "parse.key=true" \
    --property "key.separator=:"
```

## Vérifier que les messages ont été reçus

### Via kafka-console-consumer

```bash
# Lire les utilisateurs
docker exec -it kafka kafka-console-consumer \
    --bootstrap-server localhost:9092 \
    --topic user-registered \
    --from-beginning

# Lire les jeux
docker exec -it kafka kafka-console-consumer \
    --bootstrap-server localhost:9092 \
    --topic game-metadata-updated \
    --from-beginning

# Lire les patches
docker exec -it kafka kafka-console-consumer \
    --bootstrap-server localhost:9092 \
    --topic game-patched \
    --from-beginning
```

### Via Kafka UI

Ouvrir http://localhost:8080 et naviguer vers les topics.

### Via les logs du Platform Service

Les événements consommés apparaîtront dans les logs du service :

```
INFO ... UserRegisteredConsumer : Utilisateur reçu: john_doe
INFO ... GameMetadataConsumer : Métadonnées de jeu reçues: Cyberpunk 2077
INFO ... GamePatchedConsumer : Patch reçu pour game-001: version 1.5.2
```

## Vérifier les données dans la base

### H2 Console

http://localhost:8083/h2-console
- JDBC URL: `jdbc:h2:mem:platform_db`
- Username: `sa`
- Password: (vide)

```sql
SELECT * FROM platform_users;
SELECT * FROM platform_games;
SELECT * FROM platform_purchases;
```

### PostgreSQL (si configuré)

```bash
docker exec -it postgres psql -U platformuser -d platformdb

SELECT * FROM platform_users;
SELECT * FROM platform_games;
SELECT * FROM platform_purchases;
```

## Exemples de données

### Utilisateur
```json
{
  "userId": "user-001",
  "username": "john_doe",
  "email": "john.doe@example.com",
  "registrationTimestamp": 1705598400000
}
```

### Jeu
```json
{
  "gameId": "game-001",
  "title": "Cyberpunk 2077",
  "publisher": "CD Projekt Red",
  "genre": "RPG",
  "platform": "PC",
  "description": "Open-world action-adventure RPG",
  "eventTimestamp": 1705598400000
}
```

### Patch
```json
{
  "gameId": "game-001",
  "version": "1.5.2",
  "patchTimestamp": 1705598400000
}
```

## Troubleshooting

### Erreur "Connection refused" lors de l'envoi

→ Vérifier que Kafka est démarré :
```bash
docker ps | grep kafka
docker logs kafka
```

### Les messages n'arrivent pas au Platform Service

→ Vérifier que le service tourne :
```bash
# Vérifier si le port 8083 est ouvert
curl http://localhost:8083/api/health

# Voir les logs
docker logs platform-service  # Si en Docker
# ou voir les logs Gradle dans le terminal
```

### Format JSON invalide

→ S'assurer que les guillemets et accolades sont correctement échappés dans le shell :
- Bash: Utiliser des simples quotes `'...'` autour du JSON
- PowerShell: Utiliser `@'...'@` pour les chaînes multilignes
