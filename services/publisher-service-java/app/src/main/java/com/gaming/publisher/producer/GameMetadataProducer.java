package com.gaming.publisher.producer;

import com.gaming.publisher.dto.GameMetadataUpdatedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Producteur Kafka pour les événements de mise à jour de métadonnées.
 *
 * RESPONSABILITÉ : Publier sur le topic "game-metadata-updated" lorsqu'un
 * jeu voit ses métadonnées modifiées (titre, genre, description, etc.)
 *
 * REFACTORING: Injection directe des paramètres Kafka pour éviter
 * les références circulaires avec Spring beans.
 */
@Component
public class GameMetadataProducer extends BaseKafkaProducer<GameMetadataUpdatedEvent> {

    /**
     * Constructeur avec injection de dépendances Spring.
     * Les paramètres sont injectés directement depuis application.properties.
     *
     * @param bootstrapServers Adresse du cluster Kafka
     * @param schemaRegistryUrl URL du Schema Registry
     * @param topicName Nom du topic
     */
    public GameMetadataProducer(
            @Value("${kafka.bootstrap.servers}") String bootstrapServers,
            @Value("${kafka.schema.registry.url}") String schemaRegistryUrl,
            @Value("${kafka.topic.game-metadata-updated}") String topicName) {
        super(bootstrapServers, schemaRegistryUrl, topicName);
    }

    /**
     * Méthode utilitaire pour publier une mise à jour de métadonnées.
     *
     * @param event Événement de mise à jour
     */
    public void publishMetadataUpdate(GameMetadataUpdatedEvent event) {
        sendAsync(event.getGameId(), event);
    }
}

