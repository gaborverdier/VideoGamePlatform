package com.gaming.platform.consumer;

import com.gaming.events.*;
import com.gaming.platform.model.Game;
import com.gaming.platform.repository.GameRepository;
import lombok.extern.slf4j. Slf4j;
import org. apache.avro.specific. SpecificRecordBase;
import org.apache.kafka.clients. consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework. context.event.EventListener;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.math.BigDecimal;
import java.time.Duration;
import java.time. Instant;
import java.time.LocalDateTime;
import java. time.ZoneOffset;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util. concurrent.Executors;
import java.util.concurrent.atomic. AtomicBoolean;

@Component
@Slf4j
public class EventConsumer {
    
    private final Properties consumerProperties;
    private final GameRepository gameRepository;
    private final ExecutorService executorService;
    private final AtomicBoolean running = new AtomicBoolean(false);
    
    private KafkaConsumer<String, SpecificRecordBase> consumer;
    
    private static final String GAME_UPDATED_TOPIC = "game-updated";
    private static final String GAME_PATCH_RELEASED_TOPIC = "game-patch-released";
    private static final String GAME_AVAILABILITY_CHANGED_TOPIC = "game-availability-changed";
    
    public EventConsumer(Properties consumerProperties, GameRepository gameRepository) {
        this.consumerProperties = consumerProperties;
        this.gameRepository = gameRepository;
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
        if (consumer != null) {
            consumer.close();
        }
        executorService.shutdown();
        log.info("Kafka Consumer closed");
    }
    
    private void consumeEvents() {
        consumer = new KafkaConsumer<>(consumerProperties);
        consumer.subscribe(Arrays.asList(
            GAME_UPDATED_TOPIC,
            GAME_PATCH_RELEASED_TOPIC,
            GAME_AVAILABILITY_CHANGED_TOPIC
        ));
        
        log.info("📥 Subscribed to topics: {}", consumer.subscription());
        
        while (running.get()) {
            try {
                ConsumerRecords<String, SpecificRecordBase> records = 
                    consumer. poll(Duration.ofMillis(1000));
                
                for (ConsumerRecord<String, SpecificRecordBase> record :  records) {
                    processRecord(record);
                }
                
                if (!records.isEmpty()) {
                    consumer.commitSync();
                }
                
            } catch (Exception e) {
                log.error("❌ Error consuming events", e);
            }
        }
    }
    
    private void processRecord(ConsumerRecord<String, SpecificRecordBase> record) {
        String topic = record.topic();
        
        try {
            switch (topic) {
                case GAME_UPDATED_TOPIC:
                    handleGameUpdated((GameUpdated) record.value());
                    break;
                case GAME_PATCH_RELEASED_TOPIC:
                    handleGamePatchReleased((GamePatchReleased) record.value());
                    break;
                case GAME_AVAILABILITY_CHANGED_TOPIC:
                    handleGameAvailabilityChanged((GameAvailabilityChanged) record.value());
                    break;
                default:
                    log.warn("⚠️ Unknown topic: {}", topic);
            }
        } catch (Exception e) {
            log.error("❌ Error processing record from topic: {}", topic, e);
        }
    }
    
    private void handleGameUpdated(GameUpdated event) {
        log.info("📥 Received GameUpdated event for game: {}", event.getTitle());
        
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
                    Instant. ofEpochMilli(event.getUpdateTimestamp()),
                    ZoneOffset.UTC
                ));
                
                gameRepository.save(game);
                log.info("✅ Updated game: {}", game.getTitle());
            },
            () -> {
                // Create new game if it doesn't exist
                Game newGame = new Game();
                newGame.setGameId(gameId);
                newGame.setTitle(event.getTitle().toString());
                newGame.setPublisher(event.getPublisher().toString());
                newGame.setPlatform(event.getPlatform().toString());
                newGame. setGenre(event.getGenre().toString());
                newGame.setPrice(BigDecimal. valueOf(event.getPrice()));
                newGame.setVersion(event.getVersion().toString());
                newGame.setAvailable(true);
                
                if (event.getDescription() != null) {
                    newGame. setDescription(event.getDescription().toString());
                }
                
                newGame.setLastUpdated(LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(event.getUpdateTimestamp()),
                    ZoneOffset.UTC
                ));
                
                gameRepository.save(newGame);
                log. info("✅ Created new game: {}", newGame.getTitle());
            }
        );
    }
    
    private void handleGamePatchReleased(GamePatchReleased event) {
        log.info("📥 Received GamePatchReleased event for game: {} (v{} -> v{})", 
                event.getGameTitle(), event.getPreviousVersion(), event.getNewVersion());
        
        String gameId = event.getGameId().toString();
        gameRepository. findById(gameId).ifPresent(game -> {
            game.setVersion(event.getNewVersion().toString());
            game.setLastUpdated(LocalDateTime.ofInstant(
                Instant.ofEpochMilli(event.getReleaseTimestamp()),
                ZoneOffset.UTC
            ));
            
            gameRepository.save(game);
            log.info("✅ Updated game {} to version {}", 
                    game.getTitle(), event.getNewVersion());
            
            if (event.getPatchNotes() != null) {
                log.info("📝 Patch notes: {}", event.getPatchNotes());
            }
        });
    }
    
    private void handleGameAvailabilityChanged(GameAvailabilityChanged event) {
        log.info("📥 Received GameAvailabilityChanged event for game:  {} (available: {})", 
                event.getGameTitle(), event.getAvailable());
        
        String gameId = event.getGameId().toString();
        gameRepository.findById(gameId).ifPresent(game -> {
            game.setAvailable(event. getAvailable());
            game.setLastUpdated(LocalDateTime. ofInstant(
                Instant.ofEpochMilli(event.getChangeTimestamp()),
                ZoneOffset.UTC
            ));
            
            gameRepository.save(game);
            log.info("✅ Updated game {} availability to {}", 
                    game. getTitle(), event.getAvailable());
            
            if (event.getReason() != null) {
                log.info("📝 Reason: {}", event. getReason());
            }
        });
    }
}