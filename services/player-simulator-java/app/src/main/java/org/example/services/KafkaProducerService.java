
package org.example.services;

import com.gaming.events.GameCrashReported;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import io.confluent.kafka.serializers.KafkaAvroSerializer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class KafkaProducerService {

    private final KafkaProducer<String, SpecificRecordBase> producer;
    private static final String TOPIC_CRASH_REPORTED = "game-crash-reported";

    public KafkaProducerService(String bootstrapServers, String schemaRegistryUrl) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        props.put("schema.registry.url", schemaRegistryUrl);
        this.producer = new KafkaProducer<>(props);
    }

    /**
     * Publish GameCrashReported Avro event
     */
    public void publishGameCrashReported(String crashId, String userId, String gameId, String gameVersion, String platform, String errorMessage, long crashTimestamp) {
        try {
            GameCrashReported event = GameCrashReported.newBuilder()
                    .setCrashId(crashId)
                    .setUserId(userId)
                    .setGameId(gameId)
                    .setGameVersion(gameVersion)
                    .setPlatform(platform)
                    .setErrorMessage(errorMessage)
                    .setCrashTimestamp(crashTimestamp)
                    .build();
            sendEvent(TOPIC_CRASH_REPORTED, crashId, event);
            System.out.println("Published GameCrashReported event for crashId: " + crashId);
        } catch (Exception e) {
            System.err.println("Error publishing GameCrashReported event: " + e.getMessage());
            throw new RuntimeException("Failed to publish GameCrashReported event", e);
        }
    }

    /**
     * Generic method to send any Avro event
     */
    private void sendEvent(String topic, String key, SpecificRecordBase event) {
        try {
            ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(topic, key, event);
            RecordMetadata metadata = producer.send(record).get();
            System.out.printf("Event sent to topic: %s, partition: %d, offset: %d%n", topic, metadata.partition(), metadata.offset());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Interrupted while sending event to topic: " + topic);
            throw new RuntimeException("Failed to send event to Kafka", e);
        } catch (ExecutionException e) {
            System.err.println("Error sending event to topic: " + topic + ", " + e.getMessage());
            throw new RuntimeException("Failed to send event to Kafka", e);
        }
    }

    public void close() {
        if (producer != null) {
            producer.close();
            System.out.println("Kafka Producer closed");
        }
    }
}
