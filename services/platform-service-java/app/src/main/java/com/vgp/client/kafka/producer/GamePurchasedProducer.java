package com.vgp.client.kafka.producer;

import com.gaming.events.GamePurchasedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Mock Kafka producer for GamePurchasedEvent
 * In production with Kafka available, this will use actual KafkaProducer
 * For now, it logs events to demonstrate the flow
 */
@Component
public class GamePurchasedProducer {
    
    private static final Logger logger = LoggerFactory.getLogger(GamePurchasedProducer.class);
    
    private final String topic;
    
    public GamePurchasedProducer(@Value("${kafka.topic.game-purchased}") String topic) {
        this.topic = topic;
        logger.info("GamePurchasedProducer initialized with topic: {} (MOCK MODE - Kafka disabled)", topic);
    }
    
    public void publish(GamePurchasedEvent event) {
        try {
            String key = event.getGameId().toString();
            
            // Mock publishing - in production this would send to Kafka
            logger.info("MOCK: Would publish GamePurchasedEvent to topic '{}' - Purchase: {}, Game: {}, User: {}, Price: {}", 
                        topic, event.getPurchaseId(), event.getGameTitle(), event.getUserId(), event.getPrice());
            
            // Simulate async callback success
            logger.info("MOCK: Successfully published GamePurchasedEvent - Purchase: {}, Game: {}", 
                        event.getPurchaseId(), event.getGameTitle());
            
        } catch (Exception e) {
            logger.error("Error publishing GamePurchasedEvent: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to publish game purchased event", e);
        }
    }
    
    public void close() {
        logger.info("GamePurchasedProducer closed (MOCK MODE)");
    }
}
