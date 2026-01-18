"""
Script pour envoyer des événements de test vers Kafka avec sérialisation Avro.

Usage:
    python send_test_events.py --user
    python send_test_events.py --game
    python send_test_events.py --patch
    python send_test_events.py --all
"""

from confluent_kafka import Producer
from confluent_kafka.serialization import SerializationContext, MessageField
from confluent_kafka.schema_registry import SchemaRegistryClient
from confluent_kafka.schema_registry.avro import AvroSerializer
import argparse
import time

# Configuration
KAFKA_BOOTSTRAP_SERVERS = 'localhost:9092'
SCHEMA_REGISTRY_URL = 'http://localhost:8081'

# Schémas Avro (basés sur vos fichiers .avsc)
USER_SCHEMA = """
{
  "type": "record",
  "name": "UserRegistered",
  "namespace": "com.gaming.events",
  "fields": [
    {"name": "userId", "type": "string"},
    {"name": "username", "type": "string"},
    {"name": "email", "type": "string"},
    {"name": "registrationTimestamp", "type": "long"}
  ]
}
"""

GAME_METADATA_SCHEMA = """
{
  "type": "record",
  "name": "GameMetadataUpdatedEvent",
  "namespace": "com.gaming.events",
  "fields": [
    {"name": "gameId", "type": "string"},
    {"name": "title", "type": "string"},
    {"name": "publisher", "type": ["null", "string"], "default": null},
    {"name": "genre", "type": ["null", "string"], "default": null},
    {"name": "platform", "type": ["null", "string"], "default": null},
    {"name": "description", "type": ["null", "string"], "default": null},
    {"name": "eventTimestamp", "type": "long"}
  ]
}
"""

GAME_PATCH_SCHEMA = """
{
  "type": "record",
  "name": "GamePatchedEvent",
  "namespace": "com.gaming.events",
  "fields": [
    {"name": "gameId", "type": "string"},
    {"name": "version", "type": "string"},
    {"name": "patchTimestamp", "type": "long"}
  ]
}
"""

def delivery_report(err, msg):
    """Callback pour confirmer l'envoi du message."""
    if err is not None:
        print(f'❌ Erreur d\'envoi: {err}')
    else:
        print(f'✓ Message envoyé sur {msg.topic()} [partition {msg.partition()}]')

def send_user_registered():
    """Envoie un événement d'inscription utilisateur."""
    print("\n📤 Envoi d'un événement user-registered...")
    
    schema_registry_client = SchemaRegistryClient({'url': SCHEMA_REGISTRY_URL})
    avro_serializer = AvroSerializer(schema_registry_client, USER_SCHEMA)
    
    producer = Producer({'bootstrap.servers': KAFKA_BOOTSTRAP_SERVERS})
    
    # Exemple d'utilisateur
    user = {
        "userId": "user-001",
        "username": "john_doe",
        "email": "john.doe@example.com",
        "registrationTimestamp": int(time.time() * 1000)
    }
    
    producer.produce(
        topic='user-registered',
        key=user['userId'],
        value=avro_serializer(user, SerializationContext('user-registered', MessageField.VALUE)),
        on_delivery=delivery_report
    )
    
    producer.flush()
    print("✅ Utilisateur envoyé avec succès")

def send_game_metadata():
    """Envoie un événement de métadonnées de jeu."""
    print("\n📤 Envoi d'un événement game-metadata-updated...")
    
    schema_registry_client = SchemaRegistryClient({'url': SCHEMA_REGISTRY_URL})
    avro_serializer = AvroSerializer(schema_registry_client, GAME_METADATA_SCHEMA)
    
    producer = Producer({'bootstrap.servers': KAFKA_BOOTSTRAP_SERVERS})
    
    # Exemple de jeu
    game = {
        "gameId": "game-001",
        "title": "Cyberpunk 2077",
        "publisher": "CD Projekt Red",
        "genre": "RPG",
        "platform": "PC",
        "description": "Open-world action-adventure RPG set in Night City",
        "eventTimestamp": int(time.time() * 1000)
    }
    
    producer.produce(
        topic='game-metadata-updated',
        key=game['gameId'],
        value=avro_serializer(game, SerializationContext('game-metadata-updated', MessageField.VALUE)),
        on_delivery=delivery_report
    )
    
    producer.flush()
    print("✅ Métadonnées du jeu envoyées avec succès")

def send_game_patch():
    """Envoie un événement de patch de jeu."""
    print("\n📤 Envoi d'un événement game-patched...")
    
    schema_registry_client = SchemaRegistryClient({'url': SCHEMA_REGISTRY_URL})
    avro_serializer = AvroSerializer(schema_registry_client, GAME_PATCH_SCHEMA)
    
    producer = Producer({'bootstrap.servers': KAFKA_BOOTSTRAP_SERVERS})
    
    # Exemple de patch
    patch = {
        "gameId": "game-001",
        "version": "1.5.2",
        "patchTimestamp": int(time.time() * 1000)
    }
    
    producer.produce(
        topic='game-patched',
        key=patch['gameId'],
        value=avro_serializer(patch, SerializationContext('game-patched', MessageField.VALUE)),
        on_delivery=delivery_report
    )
    
    producer.flush()
    print("✅ Patch envoyé avec succès")

def send_multiple_examples():
    """Envoie plusieurs exemples de chaque type."""
    print("\n📤 Envoi de plusieurs événements de test...")
    
    # 3 utilisateurs
    users = [
        {"userId": "user-001", "username": "john_doe", "email": "john@example.com"},
        {"userId": "user-002", "username": "jane_smith", "email": "jane@example.com"},
        {"userId": "user-003", "username": "bob_wilson", "email": "bob@example.com"}
    ]
    
    # 3 jeux
    games = [
        {"gameId": "game-001", "title": "Cyberpunk 2077", "publisher": "CD Projekt Red", "genre": "RPG", "platform": "PC"},
        {"gameId": "game-002", "title": "The Witcher 3", "publisher": "CD Projekt Red", "genre": "RPG", "platform": "PC"},
        {"gameId": "game-003", "title": "Elden Ring", "publisher": "FromSoftware", "genre": "Action", "platform": "PC"}
    ]
    
    # 3 patches
    patches = [
        {"gameId": "game-001", "version": "1.5.2"},
        {"gameId": "game-002", "version": "4.01"},
        {"gameId": "game-003", "version": "1.10"}
    ]
    
    schema_registry_client = SchemaRegistryClient({'url': SCHEMA_REGISTRY_URL})
    producer = Producer({'bootstrap.servers': KAFKA_BOOTSTRAP_SERVERS})
    
    # Envoyer les utilisateurs
    print("\n👥 Envoi des utilisateurs...")
    user_serializer = AvroSerializer(schema_registry_client, USER_SCHEMA)
    for user in users:
        user['registrationTimestamp'] = int(time.time() * 1000)
        producer.produce(
            topic='user-registered',
            key=user['userId'],
            value=user_serializer(user, SerializationContext('user-registered', MessageField.VALUE)),
            on_delivery=delivery_report
        )
        time.sleep(0.5)
    
    # Envoyer les métadonnées de jeux
    print("\n🎮 Envoi des métadonnées de jeux...")
    game_serializer = AvroSerializer(schema_registry_client, GAME_METADATA_SCHEMA)
    for game in games:
        game['eventTimestamp'] = int(time.time() * 1000)
        game['description'] = f"Description of {game['title']}"
        producer.produce(
            topic='game-metadata-updated',
            key=game['gameId'],
            value=game_serializer(game, SerializationContext('game-metadata-updated', MessageField.VALUE)),
            on_delivery=delivery_report
        )
        time.sleep(0.5)
    
    # Envoyer les patches
    print("\n🔧 Envoi des patches...")
    patch_serializer = AvroSerializer(schema_registry_client, GAME_PATCH_SCHEMA)
    for patch in patches:
        patch['patchTimestamp'] = int(time.time() * 1000)
        producer.produce(
            topic='game-patched',
            key=patch['gameId'],
            value=patch_serializer(patch, SerializationContext('game-patched', MessageField.VALUE)),
            on_delivery=delivery_report
        )
        time.sleep(0.5)
    
    producer.flush()
    print("\n✅ Tous les événements ont été envoyés avec succès")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Envoyer des événements de test vers Kafka')
    parser.add_argument('--user', action='store_true', help='Envoyer un événement user-registered')
    parser.add_argument('--game', action='store_true', help='Envoyer un événement game-metadata-updated')
    parser.add_argument('--patch', action='store_true', help='Envoyer un événement game-patched')
    parser.add_argument('--all', action='store_true', help='Envoyer plusieurs événements de chaque type')
    
    args = parser.parse_args()
    
    if args.user:
        send_user_registered()
    elif args.game:
        send_game_metadata()
    elif args.patch:
        send_game_patch()
    elif args.all:
        send_multiple_examples()
    else:
        print("❌ Veuillez spécifier une option: --user, --game, --patch ou --all")
        parser.print_help()
