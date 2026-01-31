package org.example.services;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.example.models.Game;
import org.example.models.Notification;
import org.example.views.components.tabs.NotificationsTab;

import com.gaming.api.models.NotificationModel;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import javafx.application.Platform;

public class KafkaConsumerService {

    private final KafkaConsumer<String, Object> consumer;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final NotificationsTab notificationsTab;

    public KafkaConsumerService(String groupId, NotificationsTab notificationsTab) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
        props.put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG,
                System.getProperty("schema.registry.url", "http://localhost:8081"));
        props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, false);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        this.consumer = new KafkaConsumer<>(props);
        this.notificationsTab = notificationsTab;
    }

    /**
     * Start consuming from one or more topics in a single thread.
     */
    public void startListeningToTopics(String... topics) {
        consumer.subscribe(Arrays.asList(topics));
        running.set(true);

        new Thread(() -> {
            System.out.println("Subscribed to topics: " + consumer.subscription());
            try {
                while (running.get()) {
                    ConsumerRecords<String, Object> records = consumer.poll(Duration.ofMillis(100));

                    for (ConsumerRecord<String, Object> record : records) {
                        String topic = record.topic();
                        switch (topic) {
                            case "new-notification":
                                processNotification(record);
                                break;
                            case "game-crash-reported":
                                processGameCrash(record);
                                break;
                            default:
                                System.out.printf("Received message from unknown topic: %s, key=%s, value=%s%n",
                                        topic, record.key(), record.value());
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error in Kafka consumer loop: " + e.getMessage());
            } finally {
                consumer.close();
                System.out.println("Kafka consumer closed.");
            }
        }).start();
    }

    public void stop() {
        running.set(false);
    }

    private void processNotification(ConsumerRecord<String, Object> record) {
        Object val = record.value();
        if (val instanceof GenericRecord) {
            System.out.printf("Processing Avro GenericRecord [new-notification]: key=%s, value=%s%n",
                    record.key(), val);

            // Convert GenericRecord to NotificationModel
            NotificationModel model = convertGenericRecordToNotificationModel((GenericRecord) val);

            // Convert NotificationModel to Notification
            Notification notification = convertModelToNotification(model);

            // Si c'est une mise à jour de jeu, mettre à jour la version du jeu
            if ("GAME_UPDATE".equals(model.getType()) && model.getGameId() != null) {
                System.out.println("GAME_UPDATE notification received for game: " + model.getGameId());
                
                // Extraire la version de la description (format: "Version X.X.X")
                String description = model.getDescription();
                if (description != null && description.startsWith("Version ")) {
                    String newVersion = description.substring("Version ".length()).trim();
                    
                    // Mettre à jour le jeu dans GameDataService
                    Game game = GameDataService.getInstance().findGameById(model.getGameId());
                    if (game != null) {
                        game.addUpdate(newVersion); // Ajouter la version aux mises à jour disponibles
                        System.out.println("Updated game " + model.getGameId() + " to version: " + newVersion);
                    } else {
                        System.err.println("Game " + model.getGameId() + " not found in local cache");
                    }
                } else {
                    System.err.println("Could not extract version from notification description: " + description);
                }
            }

            // Add the notification to the NotificationsTab
            Platform.runLater(() -> notificationsTab.addNotificationToBeginning(notification));
        } else {
            System.out.printf("Received non-Avro message [new-notification]: key=%s, value=%s%n",
                    record.key(), val);
        }
    }

    private NotificationModel convertGenericRecordToNotificationModel(GenericRecord record) {
        // Map fields from GenericRecord to NotificationModel
        NotificationModel model = new NotificationModel();
        model.setNotificationId(record.get("notificationId").toString());
        model.setDescription(record.get("description").toString());
        model.setDate((Long) record.get("date"));
        model.setUserId(record.get("userId").toString());
        
        // Add new fields if they exist in the record
        if (record.get("type") != null) {
            model.setType(record.get("type").toString());
        }
        if (record.get("gameId") != null) {
            model.setGameId(record.get("gameId").toString());
        }
        if (record.get("gameName") != null) {
            model.setGameName(record.get("gameName").toString());
        }
        if (record.get("title") != null) {
            model.setTitle(record.get("title").toString());
        }
        
        return model;
    }

    private Notification convertModelToNotification(NotificationModel model) {
        // Use structured fields from NotificationModel
        String type = model.getType();
        String gameId = model.getGameId();
        String gameName = model.getGameName() != null ? model.getGameName() : "Notification";
        String title = model.getTitle() != null ? model.getTitle() : gameName;
        String description = model.getDescription();
        
        // Map type string to Notification.Type enum
        Notification.Type notifType = Notification.Type.NEW_REVIEW; // default
        if (type != null) {
            switch (type) {
                case "GAME_UPDATE":
                    notifType = Notification.Type.GAME_UPDATE;
                    break;
                case "GAME_DLC":
                    notifType = Notification.Type.GAME_DLC;
                    break;
                case "PRICE_DROP":
                    notifType = Notification.Type.PRICE_DROP;
                    break;
                case "NEW_REVIEW":
                    notifType = Notification.Type.NEW_REVIEW;
                    break;
            }
        }
        
        return new Notification(
            model.getNotificationId(),
            notifType,
            gameName,
            title + " - " + description,
            model.getDate(),
            false,
            gameId
        );
    }

    private void processGameCrash(ConsumerRecord<String, Object> record) {
        Object val = record.value();
        if (val instanceof GenericRecord) {
            System.out.printf("Processing Avro GenericRecord [game-crash-reported]: key=%s, value=%s%n",
                    record.key(), val);
        } else {
            System.out.printf("Processing message [game-crash-reported]: key=%s, value=%s%n",
                    record.key(), val);
        }
        // Additional processing logic for game-crash-reported here
    }
}
