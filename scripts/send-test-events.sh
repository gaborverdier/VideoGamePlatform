#!/bin/bash
# Script pour envoyer des événements de test vers Kafka (JSON simple)

echo "==== Envoi d'événements de test vers Kafka ===="
echo ""

# Fonction pour envoyer un message
send_message() {
    local topic=$1
    local key=$2
    local message=$3
    
    echo "$key:$message" | docker exec -i kafka kafka-console-producer \
        --bootstrap-server localhost:9092 \
        --topic "$topic" \
        --property "parse.key=true" \
        --property "key.separator=:"
}

# 1. Nouvel utilisateur
echo "1. Envoi d'un utilisateur..."
send_message "user-registered" "user-001" \
    '{"userId":"user-001","username":"john_doe","email":"john@example.com","registrationTimestamp":1705598400000}'
echo "✓ Utilisateur envoyé"
sleep 1

# 2. Nouveau jeu
echo "2. Envoi des métadonnées d'un jeu..."
send_message "game-metadata-updated" "game-001" \
    '{"gameId":"game-001","title":"Cyberpunk 2077","publisher":"CD Projekt Red","genre":"RPG","platform":"PC","description":"Open-world RPG","eventTimestamp":1705598400000}'
echo "✓ Jeu envoyé"
sleep 1

# 3. Patch de jeu
echo "3. Envoi d'un patch..."
send_message "game-patched" "game-001" \
    '{"gameId":"game-001","version":"1.5.2","patchTimestamp":1705598400000}'
echo "✓ Patch envoyé"
sleep 1

# Envoyer plusieurs événements
echo ""
echo "4. Envoi d'événements multiples..."

# 3 utilisateurs
send_message "user-registered" "user-002" \
    '{"userId":"user-002","username":"jane_smith","email":"jane@example.com","registrationTimestamp":1705598401000}'
send_message "user-registered" "user-003" \
    '{"userId":"user-003","username":"bob_wilson","email":"bob@example.com","registrationTimestamp":1705598402000}'

# 2 jeux supplémentaires
send_message "game-metadata-updated" "game-002" \
    '{"gameId":"game-002","title":"The Witcher 3","publisher":"CD Projekt Red","genre":"RPG","platform":"PC","description":"Fantasy RPG","eventTimestamp":1705598403000}'
send_message "game-metadata-updated" "game-003" \
    '{"gameId":"game-003","title":"Elden Ring","publisher":"FromSoftware","genre":"Action","platform":"PC","description":"Souls-like game","eventTimestamp":1705598404000}'

# Patches supplémentaires
send_message "game-patched" "game-002" \
    '{"gameId":"game-002","version":"4.01","patchTimestamp":1705598405000}'
send_message "game-patched" "game-003" \
    '{"gameId":"game-003","version":"1.10","patchTimestamp":1705598406000}'

echo "✓ Tous les événements multiples envoyés"
echo ""
echo "==== Envoi terminé ===="
echo "Vérifiez les logs du Platform Service pour voir les événements traités"
