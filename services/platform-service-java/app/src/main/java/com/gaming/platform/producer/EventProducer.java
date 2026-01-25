package com.gaming.platform.producer;

import java.time.ZoneOffset;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.gaming.api.models.NotificationModel;
import com.gaming.events.GamePurchased;
import com.gaming.events.UserLogin;
import com.gaming.events.UserRegistered;
import com.gaming.platform.model.Game;
import com.gaming.platform.model.Purchase;
import com.gaming.platform.model.User;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class EventProducer {

    private final Properties producerProperties;
    private KafkaProducer<String, SpecificRecordBase> producer;

    private static final String USER_REGISTERED_TOPIC = "user-registered";
    private static final String GAME_PURCHASED_TOPIC = "game-purchased";
    private static final String USER_LOGIN_TOPIC = "user-login";
    private static final String NOTIFICATION_TOPIC = "new-notification";

    public EventProducer(@Qualifier("producerProperties") Properties producerProperties) {
        this.producerProperties = producerProperties;
    }

    @PostConstruct
    public void init() {
        this.producer = new KafkaProducer<>(producerProperties);
        log.info("✅ Kafka Producer initialized successfully");
    }

    @PreDestroy
    public void cleanup() {
        if (producer != null) {
            producer.close();
            log.info("Kafka Producer closed");
        }
    }

    public void publishNotification(NotificationModel notification) {
        // Placeholder for notification publishing logic
        try {
 
            sendEvent(NOTIFICATION_TOPIC, notification.getUserId(), notification);
            log.info("Notification published to topic {} for user {}", NOTIFICATION_TOPIC, notification.getUserId());
        } catch (Exception e) {
            log.error("Error creating notification for user {}", notification.getUserId(), e);
            throw new RuntimeException("Failed to create notification", e);
        }
        log.info("Notification sent to user {}: {}", notification.getUserId(), notification.getDescription());
    }

    /**
     * Publish UserRegistered event
     */
    public void publishUserRegistered(User user) {
        try {
            UserRegistered event = UserRegistered.newBuilder()
                    .setUserId(user.getUserId())
                    .setUsername(user.getUsername())
                    .setEmail(user.getEmail())
                    .setRegistrationTimestamp(user.getRegistrationDate()
                            .toInstant(ZoneOffset.UTC).toEpochMilli())
                    .setCountry(user.getCountry())
                    .build();

            sendEvent(USER_REGISTERED_TOPIC, user.getUserId(), event);
            log.info("📤 Published UserRegistered event for user: {}", user.getUsername());

        } catch (Exception e) {
            log.error("❌ Error publishing UserRegistered event", e);
            throw new RuntimeException("Failed to publish UserRegistered event", e);
        }
    }

    /**
     * Publish GamePurchased event
     */
    public void publishGamePurchased(Purchase purchase, User user, Game game) {
        try {
            GamePurchased event = GamePurchased.newBuilder()
                    .setPurchaseId(purchase.getPurchaseId())
                    .setUserId(purchase.getUserId())
                    .setUsername(user.getUsername())
                    .setGameId(purchase.getGameId())
                    .setGameTitle(game.getTitle())
                    .setPrice(purchase.getPrice().doubleValue())
                    .setPurchaseTimestamp(purchase.getPurchaseDate()
                            .toInstant(ZoneOffset.UTC).toEpochMilli())
                    .setPaymentMethod(purchase.getPaymentMethod())
                    .setRegion(purchase.getRegion())
                    .build();

            sendEvent(GAME_PURCHASED_TOPIC, purchase.getPurchaseId(), event);
            log.info("📤 Published GamePurchased event:  User={}, Game={}",
                    user.getUsername(), game.getTitle());

        } catch (Exception e) {
            log.error("❌ Error publishing GamePurchased event", e);
            throw new RuntimeException("Failed to publish GamePurchased event", e);
        }
    }

    /**
     * Publish UserLogin event
     */
    public void publishUserLogin(User user, String ipAddress) {
        try {
            UserLogin event = UserLogin.newBuilder()
                    .setUserId(user.getUserId())
                    .setUsername(user.getUsername())
                    .setLoginTimestamp(System.currentTimeMillis())
                    .setIpAddress(ipAddress)
                    .setCountry(user.getCountry())
                    .build();

            sendEvent(USER_LOGIN_TOPIC, user.getUserId(), event);
            log.info("📤 Published UserLogin event for user: {}", user.getUsername());

        } catch (Exception e) {
            log.error("❌ Error publishing UserLogin event", e);
            // Don't throw - login should succeed even if event fails
        }
    }

    /**
     * Generic method to send any Avro event
     */
    private void sendEvent(String topic, String key, SpecificRecordBase event) {
        try {
            ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(topic, key, event);

            RecordMetadata metadata = producer.send(record).get();

            log.debug("✅ Event sent to topic: {}, partition: {}, offset: {}",
                    topic, metadata.partition(), metadata.offset());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("❌ Interrupted while sending event to topic: {}", topic, e);
            throw new RuntimeException("Failed to send event to Kafka", e);
        } catch (ExecutionException e) {
            log.error("❌ Error sending event to topic: {}", topic, e);
            throw new RuntimeException("Failed to send event to Kafka", e);
        }
    }

}