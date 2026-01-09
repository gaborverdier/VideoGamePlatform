package com.gaming.publisher.producer;

import com.gaming.publisher.dto.GamePatchedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Producteur Kafka pour les événements de patch de jeu.
 *
 * RESPONSABILITÉ : Publier sur le topic "game-patched" lorsqu'un
 * nouveau patch est déployé sur un jeu.
 *
 * PATTERN UTILISÉ : Héritage de BaseKafkaProducer (DRY principe)
 *
 * REFACTORING: Injection directe des paramètres Kafka pour éviter
 * les références circulaires avec Spring beans.
 */
@Component
public class GamePatchedProducer extends BaseKafkaProducer<GamePatchedEvent> {

    /**
     * Constructeur avec injection de dépendances Spring.
     * Les paramètres sont injectés directement depuis application.properties.
     *
     * @param bootstrapServers Adresse du cluster Kafka
     * @param schemaRegistryUrl URL du Schema Registry
     * @param topicName Nom du topic (depuis application.properties)
     */
    public GamePatchedProducer(
            @Value("${kafka.bootstrap.servers}") String bootstrapServers,
            @Value("${kafka.schema.registry.url}") String schemaRegistryUrl,
            @Value("${kafka.topic.game-patched}") String topicName) {
        super(bootstrapServers, schemaRegistryUrl, topicName);
    }

    /**
     * Méthode utilitaire pour publier un patch.
     *
     * @param event Événement de patch à publier
     */
    public void publishPatch(GamePatchedEvent event) {
        // Utilise le gameId comme clé pour garantir l'ordre des événements d'un même jeu
        sendAsync(event.getGameId(), event);
    }
}

