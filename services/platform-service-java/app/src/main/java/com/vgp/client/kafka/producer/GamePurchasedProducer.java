package com.vgp.client.kafka.producer;

import com.gaming.events.GamePurchasedEvent;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.Future;

@Component
public class GamePurchasedProducer {
    
    private static final Logger logger = LoggerFactory.getLogger(GamePurchasedProducer.class);
    
    private final KafkaProducer<String, GamePurchasedEvent> producer;
    private final String topic;
    
    public GamePurchasedProducer(
            Map<String, Object> producerConfigs,
            @Value("${kafka.topic.game-purchased}") String topic) {
        this.producer = new KafkaProducer<>(producerConfigs);
        this.topic = topic;
        logger.info("GamePurchasedProducer initialized with topic: {}", topic);
    }
    
    public void publish(GamePurchasedEvent event) {
        try {
            String key = event.getGameId().toString();
            ProducerRecord<String, GamePurchasedEvent> record = new ProducerRecord<>(topic, key, event);
            
            producer.send(record, (RecordMetadata metadata, Exception exception) -> {
                if (exception != null) {
                    logger.error("Failed to publish GamePurchasedEvent for purchase {}: {}", 
                                 event.getPurchaseId(), exception.getMessage(), exception);
                } else {
                    logger.info("Successfully published GamePurchasedEvent - Purchase: {}, Game: {}, Partition: {}, Offset: {}",
                                event.getPurchaseId(), event.getGameTitle(), metadata.partition(), metadata.offset());
                }
            });
        } catch (Exception e) {
            logger.error("Error publishing GamePurchasedEvent: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to publish game purchased event", e);
        }
    }
    
    public void close() {
        if (producer != null) {
            producer.close();
            logger.info("GamePurchasedProducer closed");
        }
    }
}
