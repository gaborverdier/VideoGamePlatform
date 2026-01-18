package com.gaming.platform.consumer;

import com.gaming.events.GamePatchedEvent;
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
 * Consommateur Kafka pour les événements de patch de jeu.
 *
 * RESPONSABILITÉ : Écouter le topic "game-patched" et mettre à jour
 * les versions des jeux dans le catalogue de la plateforme.
 *
 * FONCTIONNEMENT :
 * 1. S'abonne au topic au démarrage
 * 2. Pour chaque patch reçu, met à jour la version du jeu en base
 * 3. Si le jeu n'existe pas, le crée avec les informations disponibles
 */
@Component
public class GamePatchedConsumer {

    private static final Logger logger = LoggerFactory.getLogger(GamePatchedConsumer.class);

    private final KafkaConsumer<String, GamePatchedEvent> consumer;
    private final GameRepository gameRepository;
    private final ExecutorService executorService;
    private final String topicName;

    private volatile boolean running = true;

    public GamePatchedConsumer(
            GameRepository gameRepository,
            @Value("${kafka.bootstrap.servers}") String bootstrapServers,
            @Value("${kafka.schema.registry.url}") String schemaRegistryUrl,
            @Value("${kafka.consumer.group-id}") String consumerGroupId,
            @Value("${kafka.topic.game-patched}") String topicName) {

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId + "-game-patched");
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

        logger.info("GamePatchedConsumer initialisé pour le topic: {}", topicName);
    }

    @PostConstruct
    public void start() {
        consumer.subscribe(Collections.singletonList(topicName));
        executorService.submit(this::consume);
        logger.info("GamePatchedConsumer démarré et en écoute sur: {}", topicName);
    }

    private void consume() {
        try {
            while (running) {
                ConsumerRecords<String, GamePatchedEvent> records =
                    consumer.poll(Duration.ofMillis(100));

                for (ConsumerRecord<String, GamePatchedEvent> record : records) {
                    try {
                        processGamePatched(record.value());
                    } catch (Exception e) {
                        logger.error("Erreur lors du traitement de l'événement GamePatched: {}", e.getMessage(), e);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Erreur dans la boucle de consommation: {}", e.getMessage(), e);
        } finally {
            consumer.close();
            logger.info("GamePatchedConsumer fermé");
        }
    }

    /**
     * Traite un événement de patch de jeu.
     *
     * @param event L'événement reçu depuis Kafka
     */
    private void processGamePatched(GamePatchedEvent event) {
        logger.info("🔧 Patch reçu pour le jeu: {} - Version {}", 
            event.getGameTitle(), event.getVersion());

        String gameId = event.getGameId().toString();

        // Chercher le jeu en base
        Game game = gameRepository.findById(gameId).orElse(null);

        if (game == null) {
            // Le jeu n'existe pas encore, le créer
            logger.info("Création du jeu {} dans le catalogue", event.getGameTitle());
            game = Game.builder()
                .gameId(gameId)
                .title(event.getGameTitle().toString())
                .publisher(event.getPublisher().toString())
                .currentVersion(event.getVersion().toString())
                .build();
        } else {
            // Mettre à jour la version
            logger.info("Mise à jour de {} : {} → {}", 
                game.getTitle(), game.getCurrentVersion(), event.getVersion());
            game.setCurrentVersion(event.getVersion().toString());
        }

        // Sauvegarder
        gameRepository.save(game);

        logger.info("✅ Jeu {} mis à jour avec la version {}", event.getGameTitle(), event.getVersion());
    }

    @PreDestroy
    public void shutdown() {
        logger.info("Arrêt du GamePatchedConsumer...");
        running = false;
        executorService.shutdown();
    }
}
