package com.vgp.client.kafka.producer;

import com.gaming.events.GameRatingSubmittedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Mock Kafka producer for GameRatingSubmittedEvent
 * In production with Kafka available, this will use actual KafkaProducer
 * For now, it logs events to demonstrate the flow
 */
@Component
public class RatingSubmittedProducer {
    
    private static final Logger logger = LoggerFactory.getLogger(RatingSubmittedProducer.class);
    
    private final String topic;
    
    public RatingSubmittedProducer(@Value("${kafka.topic.game-rating-submitted}") String topic) {
        this.topic = topic;
        logger.info("RatingSubmittedProducer initialized with topic: {} (MOCK MODE - Kafka disabled)", topic);
    }
    
    public void publish(GameRatingSubmittedEvent event) {
        try {
            String key = event.getGameId().toString();
            
            // Mock publishing - in production this would send to Kafka
            logger.info("MOCK: Would publish GameRatingSubmittedEvent to topic '{}' - Rating: {}, Game: {}, Score: {}", 
                        topic, event.getRatingId(), event.getGameId(), event.getRating());
            
            // Simulate async callback success
            logger.info("MOCK: Successfully published GameRatingSubmittedEvent - Rating: {}, Game: {}, Score: {}",
                        event.getRatingId(), event.getGameId(), event.getRating());
            
        } catch (Exception e) {
            logger.error("Error publishing GameRatingSubmittedEvent: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to publish game rating event", e);
        }
    }
    
    public void close() {
        logger.info("RatingSubmittedProducer closed (MOCK MODE)");
    }
}
