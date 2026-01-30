package com.services;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import com.gaming.api.models.CrashAggregationModel;
import com.model.CrashAggregation;
import com.model.Game;
import com.views.components.tabs.NotificationsTab;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import javafx.application.Platform;

/**
 * Service Kafka Consumer pour l'interface JavaFX du publisher.
 * Écoute les événements de crash et les affiche en temps réel.
 */
public class PublisherKafkaConsumerService {

    private static final String CRASH_AGGREGATED_TOPIC = "crash-aggregated";
    
    private final KafkaConsumer<String, Object> consumer;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final NotificationsTab notificationsTab;
    private final List<String> publisherGameIds;
    private final String publisherId;

    public PublisherKafkaConsumerService(String publisherId, List<String> publisherGameIds, NotificationsTab notificationsTab) {
        this.publisherId = publisherId;
        this.publisherGameIds = publisherGameIds;
        this.notificationsTab = notificationsTab;
        
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        // Unique group ID per publisher to receive all messages
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "publisher-ui-" + publisherId + "-" + System.currentTimeMillis());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
        props.put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
        props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest"); // Only new messages

        this.consumer = new KafkaConsumer<>(props);
    }

    /**
     * Démarre l'écoute des crashs en arrière-plan.
     */
    public void start() {
        consumer.subscribe(Collections.singletonList(CRASH_AGGREGATED_TOPIC));
        running.set(true);

        Thread consumerThread = new Thread(() -> {
            System.out.println("[PublisherKafkaConsumer] Started listening to topic: " + CRASH_AGGREGATED_TOPIC);
            try {
                while (running.get()) {
                    ConsumerRecords<String, Object> records = consumer.poll(Duration.ofMillis(500));

                    for (ConsumerRecord<String, Object> record : records) {
                        processCrashAggregated(record);
                    }
                }
            } catch (Exception e) {
                if (running.get()) {
                    System.err.println("[PublisherKafkaConsumer] Error in consumer loop: " + e.getMessage());
                    e.printStackTrace();
                }
            } finally {
                try {
                    consumer.close();
                } catch (Exception e) {
                    // Ignore close errors
                }
                System.out.println("[PublisherKafkaConsumer] Consumer closed.");
            }
        });
        consumerThread.setDaemon(true);
        consumerThread.start();
    }

    /**
     * Arrête l'écoute Kafka.
     */
    public void stop() {
        running.set(false);
        try {
            consumer.wakeup();
        } catch (Exception e) {
            // Ignore wakeup errors
        }
    }

    /**
     * Met à jour la liste des IDs de jeux du publisher (après publication d'un nouveau jeu).
     */
    public void updateGameIds(List<String> newGameIds) {
        this.publisherGameIds.clear();
        this.publisherGameIds.addAll(newGameIds);
    }

    private void processCrashAggregated(ConsumerRecord<String, Object> record) {
        try {
            Object value = record.value();
            
            if (value instanceof CrashAggregationModel) {
                CrashAggregationModel crashModel = (CrashAggregationModel) value;
                String gameId = crashModel.getGameId();
                
                System.out.println("[PublisherKafkaConsumer] Received crash for gameId: " + gameId);
                
                // Vérifier si ce crash concerne un jeu de cet éditeur
                if (publisherGameIds.contains(gameId)) {
                    System.out.println("[PublisherKafkaConsumer] Crash matches publisher's game, adding to UI");
                    
                    // Convertir en CrashAggregation pour l'affichage
                    CrashAggregation crash = CrashAggregation.builder()
                            .id(crashModel.getId() != null ? crashModel.getId() : "crash-" + System.currentTimeMillis())
                            .gameId(gameId)
                            .crashCount(crashModel.getCrashCount())
                            .timestamp(crashModel.getTimestamp() != 0 ? crashModel.getTimestamp() : System.currentTimeMillis())
                            .windowStart(crashModel.getWindowStart() != 0 ? crashModel.getWindowStart() : System.currentTimeMillis() - 300000)
                            .windowEnd(crashModel.getWindowEnd() != 0 ? crashModel.getWindowEnd() : System.currentTimeMillis())
                            .build();
                    
                    // Créer un objet Game minimal pour l'affichage
                    Game game = new Game();
                    game.setId(gameId);
                    game.setTitle("Game " + gameId); // Sera mis à jour si on a le titre
                    crash.setGame(game);
                    
                    // Ajouter à l'interface sur le thread JavaFX
                    Platform.runLater(() -> {
                        notificationsTab.addCrashReport(crash);
                        System.out.println("[PublisherKafkaConsumer] Crash added to UI");
                    });
                } else {
                    System.out.println("[PublisherKafkaConsumer] Crash ignored (not this publisher's game). Known games: " + publisherGameIds);
                }
            } else {
                System.out.println("[PublisherKafkaConsumer] Received non-CrashAggregationModel: " + value.getClass().getName());
            }
        } catch (Exception e) {
            System.err.println("[PublisherKafkaConsumer] Error processing crash: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
