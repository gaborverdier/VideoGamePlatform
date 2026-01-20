package org.example.services;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class KafkaProducerService {

    private final KafkaProducer<String, String> producer;
    private final String TOPIC_SESSION_STARTED = "player-session-started";
    private final String TOPIC_SESSION_ENDED = "player-session-ended";
    private final String TOPIC_CRASH_REPORTED = "player-crash-reported";

    public KafkaProducerService() {
        Properties props = new Properties();
        // Default to localhost, in a real app this would be in config
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        this.producer = new KafkaProducer<>(props);
    }

    public void publishSessionStarted(String userId, String username, String gameId, String gameTitle,
            String gameVersion) {
        String jsonValue = String.format(
                "{\"userId\":\"%s\", \"username\":\"%s\", \"gameId\":\"%s\", \"gameTitle\":\"%s\", \"gameVersion\":\"%s\", \"event\":\"STARTED\"}",
                userId, username, gameId, gameTitle, gameVersion);
        sendMessage(TOPIC_SESSION_STARTED, userId, jsonValue);
    }

    public void publishSessionEnded(String sessionId, String userId, String username, String gameId, String gameTitle,
            String gameVersion, long durationSeconds) {
        String jsonValue = String.format(
                "{\"sessionId\":\"%s\", \"userId\":\"%s\", \"username\":\"%s\", \"gameId\":\"%s\", \"gameTitle\":\"%s\", \"gameVersion\":\"%s\", \"duration\":\"%d\", \"event\":\"ENDED\"}",
                sessionId, userId, username, gameId, gameTitle, gameVersion, durationSeconds);
        sendMessage(TOPIC_SESSION_ENDED, userId, jsonValue); // Use userId as key for partitioning consistency
    }

    public void publishCrashReported(String userId, String username, String gameId, String gameTitle,
            String gameVersion, String message) {
        String jsonValue = String.format(
                "{\"userId\":\"%s\", \"username\":\"%s\", \"gameId\":\"%s\", \"gameTitle\":\"%s\", \"gameVersion\":\"%s\", \"error\":\"%s\", \"event\":\"CRASH\"}",
                userId, username, gameId, gameTitle, gameVersion, message);
        sendMessage(TOPIC_CRASH_REPORTED, userId, jsonValue);
    }

    private void sendMessage(String topic, String key, String value) {
        try {
            producer.send(new ProducerRecord<>(topic, key, value));
            System.out.println("Sent message to " + topic + ": " + value);
        } catch (Exception e) {
            System.err.println("Error sending message to Kafka: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void close() {
        if (producer != null) {
            producer.close();
        }
    }
}
