package org.example.services;

import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public class KafkaConsumerService {

    private final KafkaConsumer<String, Object> consumer;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public KafkaConsumerService(String groupId) {
        Properties props = new Properties();
        // Default to localhost
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        // Use Avro deserializer and accept GenericRecord fallback
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
        props.put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, System.getProperty("schema.registry.url", "http://localhost:8081"));
        props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, false);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        this.consumer = new KafkaConsumer<>(props);
    }

    public void subscribe(String topic) {
        consumer.subscribe(Collections.singletonList(topic));
        System.out.println("Subscribed to topic: " + topic);
    }

    public void startListening() {
        running.set(true);
        // In a real app, this should likely run in a separate thread
        new Thread(() -> {
            try {
                while (running.get()) {
                    ConsumerRecords<String, Object> records = consumer.poll(Duration.ofMillis(100));
                    for (ConsumerRecord<String, Object> record : records) {
                        Object val = record.value();
                        if (val instanceof GenericRecord) {
                            System.out.printf("Received Avro GenericRecord: key=%s, value=%s%n", record.key(), val);
                        } else {
                            System.out.printf("Received message: key=%s, value=%s%n", record.key(), val);
                        }
                        // TODO: Process message
                    }
                }
            } catch (Exception e) {
                System.err.println("Error in Kafka consumer loop: " + e.getMessage());
            } finally {
                consumer.close();
                System.out.println("Kafka consumer closed.");
            }
        }).start();
    }

    public void stop() {
        running.set(false);
    }
}
