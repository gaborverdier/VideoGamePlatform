package com.vgp.client.kafka.producer;

import com.gaming.events.GameRatingSubmittedEvent;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RatingSubmittedProducer {
    
    private static final Logger logger = LoggerFactory.getLogger(RatingSubmittedProducer.class);
    
    private final KafkaProducer<String, GameRatingSubmittedEvent> producer;
    private final String topic;
    
    public RatingSubmittedProducer(
            Map<String, Object> producerConfigs,
            @Value("${kafka.topic.game-rating-submitted}") String topic) {
        this.producer = new KafkaProducer<>(producerConfigs);
        this.topic = topic;
        logger.info("RatingSubmittedProducer initialized with topic: {}", topic);
    }
    
    public void publish(GameRatingSubmittedEvent event) {
        try {
            String key = event.getGameId().toString();
            ProducerRecord<String, GameRatingSubmittedEvent> record = new ProducerRecord<>(topic, key, event);
            
            producer.send(record, (RecordMetadata metadata, Exception exception) -> {
                if (exception != null) {
                    logger.error("Failed to publish GameRatingSubmittedEvent for rating {}: {}", 
                                 event.getRatingId(), exception.getMessage(), exception);
                } else {
                    logger.info("Successfully published GameRatingSubmittedEvent - Rating: {}, Game: {}, Score: {}, Partition: {}, Offset: {}",
                                event.getRatingId(), event.getGameId(), event.getRating(), 
                                metadata.partition(), metadata.offset());
                }
            });
        } catch (Exception e) {
            logger.error("Error publishing GameRatingSubmittedEvent: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to publish game rating event", e);
        }
    }
    
    public void close() {
        if (producer != null) {
            producer.close();
            logger.info("RatingSubmittedProducer closed");
        }
    }
}
