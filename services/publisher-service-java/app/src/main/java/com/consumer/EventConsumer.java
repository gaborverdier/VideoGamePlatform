package com.consumer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.RecordDeserializationException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.gaming.events.GameAvailabilityChanged;
import com.gaming.events.GameCrashReported;
import com.gaming.events.GamePatchReleased;
import com.gaming.events.GameUpdated;
//import com.handler.CrashEventHandler;
import com.handler.GameEventHandler;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

/**
 * Consumer Kafka responsable du polling et du routing des événements.
 * La logique métier est déléguée aux handlers dédiés.
 */
@Component
@Slf4j
public class EventConsumer {

    private final Properties consumerProperties;
    //private final CrashEventHandler crashEventHandler;
    private final GameEventHandler gameEventHandler;
    private final ExecutorService executorService;
    private final AtomicBoolean running = new AtomicBoolean(false);

    private KafkaConsumer<String, Object> consumer;

    private static final String GAME_CRASH_REPORTED = "game-crash-reported";
    private static final String GAME_UPDATED_TOPIC = "game-updated";
    private static final String GAME_PATCH_RELEASED_TOPIC = "game-patch-released";
    private static final String GAME_AVAILABILITY_CHANGED_TOPIC = "game-availability-changed";

    public EventConsumer(
            @Qualifier("consumerProperties") Properties consumerProperties,
            //CrashEventHandler crashEventHandler,
            GameEventHandler gameEventHandler) {
        this.consumerProperties = consumerProperties;
        //this.crashEventHandler = crashEventHandler;
        this.gameEventHandler = gameEventHandler;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startConsuming() {
        running.set(true);
        executorService.submit(this::consumeEvents);
        log.info("✅ Started Kafka event consumer");
    }

    @PreDestroy
    public void cleanup() {
        running.set(false);
        synchronized (this) {
            if (consumer != null) {
                consumer.close();
            }
        }
        executorService.shutdown();
        log.info("Kafka Consumer closed");
    }

    private void consumeEvents() {
        consumer = new KafkaConsumer<>(consumerProperties);
        consumer.subscribe(Arrays.asList(
                GAME_CRASH_REPORTED,
                GAME_UPDATED_TOPIC,
                GAME_PATCH_RELEASED_TOPIC,
                GAME_AVAILABILITY_CHANGED_TOPIC));

        log.info("📥 Subscribed to topics: {}", consumer.subscription());

        while (running.get()) {
            try {
                ConsumerRecords<String, Object> records = consumer.poll(Duration.ofMillis(1000));

                for (ConsumerRecord<String, Object> record : records) {
                    try {
                        processRecord(record);
                    } catch (Exception e) {
                        log.error("❌ Error processing record at offset {} from topic {}: {}",
                                record.offset(), record.topic(), e.getMessage(), e);
                        // Seek past the problematic record
                        consumer.seek(new TopicPartition(record.topic(), record.partition()), record.offset() + 1);
                    }
                }

                if (!records.isEmpty()) {
                    consumer.commitSync();
                }

            } catch (RecordDeserializationException rde) {
                log.error("❌ Record deserialization error while polling - seeking past problematic record", rde);
                try {
                    TopicPartition tp = rde.topicPartition();
                    long offset = rde.offset();
                    consumer.seek(tp, offset + 1);
                } catch (Exception ex) {
                    log.error("❌ Failed to seek past deserialized record", ex);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            } catch (Exception e) {
                log.error("❌ Error consuming events", e);
            }
        }
    }

    private void processRecord(ConsumerRecord<String, Object> record) {
        String topic = record.topic();

        try {
            switch (topic) {
                case GAME_CRASH_REPORTED:
                    handleCrashReportedEvent(record.value());
                    break;
                case GAME_UPDATED_TOPIC:
                    handleGameUpdatedEvent(record.value());
                    break;
                case GAME_PATCH_RELEASED_TOPIC:
                    handlePatchReleasedEvent(record.value());
                    break;
                case GAME_AVAILABILITY_CHANGED_TOPIC:
                    handleAvailabilityChangedEvent(record.value());
                    break;
                default:
                    log.warn("⚠️ Unknown topic: {}", topic);
            }
        } catch (Exception e) {
            log.error("❌ Error processing record from topic: {}", topic, e);
            throw e; // Re-throw pour la gestion d'erreur globale
        }
    }

    // ========== Event Routing Methods ==========

    private void handleCrashReportedEvent(Object value) {
        if (value instanceof GameCrashReported) {
            //crashEventHandler.handleCrashReported((GameCrashReported) value);
        } else if (value instanceof GenericRecord) {
            //crashEventHandler.handleCrashReportedGeneric((GenericRecord) value);
        } else {
            log.warn("⚠️ Unexpected record type for GameCrashReported: {}", 
                value == null ? "null" : value.getClass());
        }
    }

    private void handleGameUpdatedEvent(Object value) {
        if (value instanceof GameUpdated) {
            gameEventHandler.handleGameUpdated((GameUpdated) value);
        } else if (value instanceof GenericRecord) {
            gameEventHandler.handleGameUpdatedGeneric((GenericRecord) value);
        } else {
            log.warn("⚠️ Unexpected record type for GameUpdated: {}", 
                value == null ? "null" : value.getClass());
        }
    }

    private void handlePatchReleasedEvent(Object value) {
        if (value instanceof GamePatchReleased) {
            gameEventHandler.handlePatchReleased((GamePatchReleased) value);
        } else if (value instanceof GenericRecord) {
            gameEventHandler.handlePatchReleasedGeneric((GenericRecord) value);
        } else {
            log.warn("⚠️ Unexpected record type for GamePatchReleased: {}", 
                value == null ? "null" : value.getClass());
        }
    }

    private void handleAvailabilityChangedEvent(Object value) {
        if (value instanceof GameAvailabilityChanged) {
            gameEventHandler.handleAvailabilityChanged((GameAvailabilityChanged) value);
        } else if (value instanceof GenericRecord) {
            gameEventHandler.handleAvailabilityChangedGeneric((GenericRecord) value);
        } else {
            log.warn("⚠️ Unexpected record type for GameAvailabilityChanged: {}", 
                value == null ? "null" : value.getClass());
        }
    }
}