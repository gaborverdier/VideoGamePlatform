package com.consumer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.RecordDeserializationException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.gaming.events.GameCrashReported;
import com.handler.CrashReportedEventHandler;

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
    private final CrashReportedEventHandler crashReportedEventHandler;
    private final ExecutorService executorService;
    private final AtomicBoolean running = new AtomicBoolean(false);

    private KafkaConsumer<String, Object> consumer;

    private static final String GAME_CRASH_REPORTED = "game-crash-reported";
    private static final String GAME_UPDATED_TOPIC = "game-updated";
    private static final String GAME_PATCH_RELEASED_TOPIC = "game-patch-released";
    private static final String GAME_AVAILABILITY_CHANGED_TOPIC = "game-availability-changed";
    private static final String DLC_RELEASED_TOPIC = "dlc-released";
    private static final String DLC_UPDATED_TOPIC = "dlc-updated";

    public EventConsumer(
            @Qualifier("consumerProperties") Properties consumerProperties,
            CrashReportedEventHandler crashReportedEventHandler) {
        this.consumerProperties = consumerProperties;
        this.crashReportedEventHandler = crashReportedEventHandler;
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
                GAME_AVAILABILITY_CHANGED_TOPIC,
                DLC_RELEASED_TOPIC,
                DLC_UPDATED_TOPIC
            ));

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

        switch (topic) {
            case GAME_CRASH_REPORTED:
                GameCrashReported crashEvent = (GameCrashReported) record.value();
                log.info("🚨 Processing GameCrashReported event: {}", crashEvent);
                crashReportedEventHandler.handle(crashEvent);
                
                break;
        
            default:
                break;
        }
    }
}