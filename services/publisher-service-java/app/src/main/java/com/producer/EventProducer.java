package com.producer;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * Classe utilitaire bas niveau pour l'envoi d'événements Kafka.
 * Ne contient aucune logique métier, uniquement la mécanique d'envoi.
 * 
 * La logique métier de création des événements est déléguée aux publishers (package com.publisher).
 */
@Component
@Slf4j
public class EventProducer {
    
    private final Properties producerProperties;
    private KafkaProducer<String, SpecificRecordBase> producer;
    
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
    
    /**
     * Envoie un événement Avro sur un topic Kafka.
     * Méthode générique utilisable par tous les publishers.
     * 
     * @param topic Le topic Kafka cible
     * @param key La clé du message (généralement un ID)
     * @param event L'événement Avro à envoyer
     * @return Les métadonnées du message envoyé
     * @throws RuntimeException si l'envoi échoue
     */
    public RecordMetadata send(String topic, String key, SpecificRecordBase event) {
        try {
            ProducerRecord<String, SpecificRecordBase> record = 
                new ProducerRecord<>(topic, key, event);
            
            RecordMetadata metadata = producer.send(record).get();
            
            log.debug("✅ Event sent to topic: {}, partition: {}, offset: {}", 
                    topic, metadata.partition(), metadata.offset());
            
            return metadata;
                    
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("❌ Interrupted while sending event to topic: {}", topic, e);
            throw new RuntimeException("Failed to send event to Kafka", e);
        } catch (ExecutionException e) {
            log.error("❌ Error sending event to topic: {}", topic, e);
            throw new RuntimeException("Failed to send event to Kafka", e);
        }
    }
    
    /**
     * Envoie un événement de manière asynchrone (fire-and-forget).
     * Utilisé pour des événements non critiques.
     * 
     * @param topic Le topic Kafka cible
     * @param key La clé du message
     * @param event L'événement Avro à envoyer
     */
    public void sendAsync(String topic, String key, SpecificRecordBase event) {
        ProducerRecord<String, SpecificRecordBase> record = 
            new ProducerRecord<>(topic, key, event);
        
        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                log.error("❌ Error sending async event to topic: {}", topic, exception);
            } else {
                log.debug("✅ Async event sent to topic: {}, partition: {}, offset: {}", 
                        topic, metadata.partition(), metadata.offset());
            }
        });
    }
}