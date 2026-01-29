package com.gaming.platform.consumer;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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

import com.gaming.api.models.GameModel;
import com.gaming.events.GameCrashReported;
import com.gaming.events.GamePatchReleased;
import com.gaming.events.GameUpdated;
import com.gaming.platform.model.CrashReport;
import com.gaming.platform.model.Game;
import com.gaming.platform.repository.GameRepository;
import com.gaming.platform.service.CrashReportService;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class EventConsumer {

    private final Properties consumerProperties;
    private final GameRepository gameRepository;
    private final CrashReportService crashReportService;
    private final ExecutorService executorService;
    private final AtomicBoolean running = new AtomicBoolean(false);

    private KafkaConsumer<String, Object> consumer;

    private static final String GAME_CRASH_REPORTED = "game-crash-reported";
    private static final String GAME_UPDATED_TOPIC = "game-updated";
    private static final String GAME_PATCH_RELEASED_TOPIC = "game-patch-released";
    private static final String GAME_RELEASED = "game-released";

    public EventConsumer(@Qualifier("consumerProperties") Properties consumerProperties,
            GameRepository gameRepository,
            CrashReportService crashReportService) {
        this.consumerProperties = consumerProperties;
        this.gameRepository = gameRepository;
        this.crashReportService = crashReportService;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startConsuming() {
        running.set(true);
        executorService.submit(this::consumeEvents);
        log.info("Started Kafka event consumer");
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
                GAME_RELEASED));

        log.info("📥 Subscribed to topics: {}", consumer.subscription());

        while (running.get()) {
            try {
                ConsumerRecords<String, Object> records = consumer.poll(Duration.ofMillis(1000));

                for (ConsumerRecord<String, Object> record : records) {
                    try {
                        processRecord(record);
                    } catch (Exception e) {
                        log.error("Error processing record at offset {} from topic {}: {}",
                                record.offset(), record.topic(), e.getMessage(), e);
                        // Seek past the problematic record
                        consumer.seek(new TopicPartition(record.topic(), record.partition()), record.offset() + 1);
                    }
                }

                if (!records.isEmpty()) {
                    consumer.commitSync();
                }

            } catch (RecordDeserializationException rde) {
                log.error("Record deserialization error while polling - seeking past problematic record", rde);
                try {
                    TopicPartition tp = rde.topicPartition();
                    long offset = rde.offset();
                    consumer.seek(tp, offset + 1);
                } catch (Exception ex) {
                    log.error("Failed to seek past deserialized record", ex);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            } catch (Exception e) {
                log.error("Error consuming events", e);
            }
        }
    }

    private void processRecord(ConsumerRecord<String, Object> record) {
        String topic = record.topic();

        try {
            switch (topic) {
                case GAME_CRASH_REPORTED:
                    Object val = record.value();
                    if (val instanceof GameCrashReported) {
                        handleGameCrashedReported((GameCrashReported) val);
                    } else if (val instanceof GenericRecord) {
                        handleGameCrashedReportedGeneric((GenericRecord) val);
                    } else {
                        log.warn("Unexpected record type for topic {}: {}", topic, val == null ? "null" : val.getClass());
                    }
                    break;
                case GAME_UPDATED_TOPIC:
                    if (record.value() instanceof GameUpdated) {
                        handleGameUpdated((GameUpdated) record.value());
                    } else if (record.value() instanceof GenericRecord) {
                        handleGameUpdatedGeneric((GenericRecord) record.value());
                    }
                    break;
                case GAME_PATCH_RELEASED_TOPIC:
                    if (record.value() instanceof GamePatchReleased) {
                        handleGamePatchReleased((GamePatchReleased) record.value());
                    } else if (record.value() instanceof GenericRecord) {
                        handleGamePatchReleasedGeneric((GenericRecord) record.value());
                    }
                    break;
                case GAME_RELEASED:
                    if (record.value() instanceof GameModel) {
                        handleGameReleased((GameModel) record.value());
                    } else if (record.value() instanceof GenericRecord) {
                        handleGameReleasedGeneric((GenericRecord) record.value());
                        
                    }
                    break;
                default:
                    log.warn("Unknown topic: {}", topic);
            }
        } catch (Exception e) {
            log.error("Error processing record from topic: {}", topic, e);
        }
    }

    // GenericRecord handlers - map fields manually when SpecificRecord class is unavailable
    private void handleGameCrashedReportedGeneric(GenericRecord event) {
        log.info("📥 Received GameCrashReported (generic) event: {}", event);
        Object crashId = event.get("crashId");
        Object userId = event.get("userId");
        Object gameId = event.get("gameId");
        Object gameVersion = event.get("gameVersion");
        Object platform = event.get("platform");
        Object errorMessage = event.get("errorMessage");
        Object crashTimestamp = event.get("crashTimestamp");

        log.info("CrashId={}, userId={}, gameId={}, version={}, platform={}, error={}, ts={}",
                crashId, userId, gameId, gameVersion, platform, errorMessage, crashTimestamp);

        try {
            CrashReport report = new CrashReport();
            report.setCrashId(crashId != null ? crashId.toString() : null);
            report.setUserId(userId != null ? userId.toString() : null);
            report.setGameId(gameId != null ? gameId.toString() : null);
            report.setGameVersion(gameVersion != null ? gameVersion.toString() : null);
            report.setPlatform(platform != null ? platform.toString() : null);
            report.setErrorMessage(errorMessage != null ? errorMessage.toString() : null);
            if (crashTimestamp != null) {
                try {
                    long ts = Long.parseLong(crashTimestamp.toString());
                    report.setCrashTimestamp(LocalDateTime.ofInstant(Instant.ofEpochMilli(ts), ZoneOffset.UTC));
                } catch (NumberFormatException nfe) {
                    log.warn("Could not parse crashTimestamp: {}", crashTimestamp);
                }
            }

            crashReportService.save(report);
            log.info("Saved crash report (generic): {}", report.getCrashId());
        } catch (Exception e) {
            log.error("Failed to save generic crash report", e);
        }
    }

    private void handleGameUpdatedGeneric(GenericRecord event) {
        log.info("Received GameUpdated (generic) event: {}", event);
    }

    private void handleGamePatchReleasedGeneric(GenericRecord event) {
        log.info("Received GamePatchReleased (generic) event: {}", event);
    }

    private void handleGameReleasedGeneric(GenericRecord event) {
        log.info("Received GameReleased (generic) event: {}", event);
    }


    private void handleGameCrashedReported(GameCrashReported event) {
        log.info("Received GameCrashReported event: {}", event.toString());
        CrashReport report = new CrashReport();
        report.setCrashId(event.getCrashId().toString());
        report.setUserId(event.getUserId() != null ? event.getUserId().toString() : null);
        report.setGameId(event.getGameId() != null ? event.getGameId().toString() : null);
        report.setGameVersion(event.getGameVersion() != null ? event.getGameVersion().toString() : null);
        report.setPlatform(event.getPlatform() != null ? event.getPlatform().toString() : null);
        report.setErrorMessage(event.getErrorMessage() != null ? event.getErrorMessage().toString() : null);
        report.setCrashTimestamp(LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getCrashTimestamp()), ZoneOffset.UTC));

        try {
            crashReportService.save(report);
            log.info("Saved crash report: {}", report.getCrashId());
        } catch (Exception e) {
            log.error("Failed to save crash report: {}", report.getCrashId(), e);
        }
    }

    private void handleGameUpdated(GameUpdated event) {
        log.info("Received GameUpdated event for game: {}", event.getTitle());

        String gameId = event.getGameId().toString();
        gameRepository.findById(gameId).ifPresentOrElse(
                game -> {
                    // Update existing game
                    game.setTitle(event.getTitle().toString());
                    game.setPublisher(event.getPublisher().toString());
                    game.setPlatform(event.getPlatform().toString());
                    game.setGenre(event.getGenre().toString());
                    game.setPrice(BigDecimal.valueOf(event.getPrice()));
                    game.setVersion(event.getVersion().toString());

                    if (event.getDescription() != null) {
                        game.setDescription(event.getDescription().toString());
                    }

                    game.setLastUpdated(LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(event.getUpdateTimestamp()),
                            ZoneOffset.UTC));

                    gameRepository.save(game);
                    log.info("Updated game: {}", game.getTitle());
                },
                () -> {
                    // Create new game if it doesn't exist
                    Game newGame = new Game();
                    newGame.setGameId(gameId);
                    newGame.setTitle(event.getTitle().toString());
                    newGame.setPublisher(event.getPublisher().toString());
                    newGame.setPlatform(event.getPlatform().toString());
                    newGame.setGenre(event.getGenre().toString());
                    newGame.setPrice(BigDecimal.valueOf(event.getPrice()));
                    newGame.setVersion(event.getVersion().toString());

                    if (event.getDescription() != null) {
                        newGame.setDescription(event.getDescription().toString());
                    }

                    newGame.setLastUpdated(LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(event.getUpdateTimestamp()),
                            ZoneOffset.UTC));

                    gameRepository.save(newGame);
                    log.info("Created new game: {}", newGame.getTitle());
                });
    }

    private void handleGamePatchReleased(GamePatchReleased event) {
        log.info("Received GamePatchReleased event for game: {} (v{} -> v{})",
                event.getGameTitle(), event.getPreviousVersion(), event.getNewVersion());

        String gameId = event.getGameId().toString();
        gameRepository.findById(gameId).ifPresent(game -> {
            game.setVersion(event.getNewVersion().toString());
            game.setLastUpdated(LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(event.getReleaseTimestamp()),
                    ZoneOffset.UTC));

            gameRepository.save(game);
            log.info("Updated game {} to version {}",
                    game.getTitle(), event.getNewVersion());

            if (event.getPatchNotes() != null) {
                log.info("Patch notes: {}", event.getPatchNotes());
            }
        });
    }

    private void handleGameReleased(GameModel event) {
        log.info("Received GameReleased event for new game: {}", event.getTitle());

        Game newGame = new Game();
        newGame.setGameId(event.getGameId().toString());
        newGame.setTitle(event.getTitle().toString());
        newGame.setPublisher(event.getPublisherName().toString());
        newGame.setPlatform(event.getPlatform().toString());
        newGame.setGenre(event.getGenre().toString());
        newGame.setPrice(BigDecimal.valueOf(event.getPrice()));
        newGame.setVersion(event.getVersion().toString());
        Long releaseTs = event.getReleaseTimeStamp();
        newGame.setReleaseTimeStamp(releaseTs);

        if (event.getDescription() != null) {
            newGame.setDescription(event.getDescription().toString());
        }

        // TODO update last updated filed here
        newGame.setLastUpdated(LocalDateTime.ofInstant(Instant.ofEpochMilli(releaseTs), ZoneOffset.UTC));

        gameRepository.save(newGame);
        log.info("Saved new game: {}", newGame.getTitle());
    }
}