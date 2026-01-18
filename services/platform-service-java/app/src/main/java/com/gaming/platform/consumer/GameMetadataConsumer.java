package com.gaming.platform.consumer;

import com.gaming.events.GameMetadataUpdatedEvent;
import com.gaming.platform.model.Game;
import com.gaming.platform.repository.GameRepository;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Consommateur Kafka pour les événements de mise à jour de métadonnées.
 *
 * RESPONSABILITÉ : Écouter le topic "game-metadata-updated" et synchroniser
 * les métadonnées des jeux dans le catalogue de la plateforme.
 *
 * FONCTIONNEMENT :
 * 1. S'abonne au topic au démarrage
 * 2. Pour chaque mise à jour reçue, synchronise les infos du jeu en base
 * 3. Si le jeu n'existe pas, le crée
 */
@Component
public class GameMetadataConsumer {

    private static final Logger logger = LoggerFactory.getLogger(GameMetadataConsumer.class);

    private final KafkaConsumer<String, GameMetadataUpdatedEvent> consumer;
    private final GameRepository gameRepository;
    private final ExecutorService executorService;
    private final String topicName;

    private volatile boolean running = true;

    public GameMetadataConsumer(
            GameRepository gameRepository,
            @Value("${kafka.bootstrap.servers}") String bootstrapServers,
            @Value("${kafka.schema.registry.url}") String schemaRegistryUrl,
            @Value("${kafka.consumer.group-id}") String consumerGroupId,
            @Value("${kafka.topic.game-metadata-updated}") String topicName) {

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId + "-game-metadata");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
        props.put("schema.registry.url", schemaRegistryUrl);
        props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 1000);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        this.consumer = new KafkaConsumer<>(props);
        this.gameRepository = gameRepository;
        this.topicName = topicName;
        this.executorService = Executors.newSingleThreadExecutor();

        logger.info("GameMetadataConsumer initialisé pour le topic: {}", topicName);
    }

    @PostConstruct
    public void start() {
        consumer.subscribe(Collections.singletonList(topicName));
        executorService.submit(this::consume);
        logger.info("GameMetadataConsumer démarré et en écoute sur: {}", topicName);
    }

    private void consume() {
        try {
            while (running) {
                ConsumerRecords<String, GameMetadataUpdatedEvent> records =
                    consumer.poll(Duration.ofMillis(100));

                for (ConsumerRecord<String, GameMetadataUpdatedEvent> record : records) {
                    try {
                        processMetadataUpdate(record.value());
                    } catch (Exception e) {
                        logger.error("Erreur lors du traitement de l'événement GameMetadataUpdated: {}", e.getMessage(), e);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Erreur dans la boucle de consommation: {}", e.getMessage(), e);
        } finally {
            consumer.close();
            logger.info("GameMetadataConsumer fermé");
        }
    }

    /**
     * Traite un événement de mise à jour de métadonnées.
     *
     * @param event L'événement reçu depuis Kafka
     */
    private void processMetadataUpdate(GameMetadataUpdatedEvent event) {
        logger.info("Métadonnées mises à jour pour le jeu: {}", event.getGameTitle());

        String gameId = event.getGameId().toString();

        // Chercher le jeu en base
        Game game = gameRepository.findById(gameId).orElse(null);

        if (game == null) {
            // Le jeu n'existe pas encore, le créer
            logger.info("Création du jeu {} dans le catalogue", event.getGameTitle());
            game = Game.builder()
                .gameId(gameId)
                .title(event.getGameTitle().toString())
                .genre(event.getGenre() != null ? event.getGenre().toString() : null)
                .platform(event.getPlatform() != null ? event.getPlatform().toString() : null)
                .publisher(event.getPublisher().toString())
                .description(event.getDescription() != null ? event.getDescription().toString() : null)
                .currentVersion("1.0.0") // Version par défaut
                .build();
        } else {
            // Mettre à jour les métadonnées
            if (event.getGenre() != null) {
                game.setGenre(event.getGenre().toString());
            }
            if (event.getPlatform() != null) {
                game.setPlatform(event.getPlatform().toString());
            }
            if (event.getDescription() != null) {
                game.setDescription(event.getDescription().toString());
            }
            logger.info("Mise à jour des métadonnées de {}", game.getTitle());
        }

        // Sauvegarder
        gameRepository.save(game);

        logger.info("Métadonnées de {} synchronisées", event.getGameTitle());
    }

    @PreDestroy
    public void shutdown() {
        logger.info("Arrêt du GameMetadataConsumer...");
        running = false;
        executorService.shutdown();
    }
}
