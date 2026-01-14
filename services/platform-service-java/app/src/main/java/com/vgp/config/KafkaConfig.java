package com.vgp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Configuration
 * NOTE: This is currently in MOCK MODE due to network restrictions
 * When deploying to production:
 * 1. Uncomment Kafka dependencies in build.gradle.kts
 * 2. Uncomment the actual Kafka configuration imports
 * 3. Replace mock producers with real KafkaProducer instances
 */
@Configuration
public class KafkaConfig {

    @Value("${kafka.bootstrap.servers}")
    private String bootstrapServers;

    @Value("${kafka.schema.registry.url}")
    private String schemaRegistryUrl;

    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        // Mock configuration - in production, uncomment the actual Kafka config
        props.put("bootstrap.servers", bootstrapServers);
        props.put("schema.registry.url", schemaRegistryUrl);
        
        // These would be the actual producer configs:
        // props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        // props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        // props.put("schema.registry.url", schemaRegistryUrl);
        // props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        // props.put(ProducerConfig.ACKS_CONFIG, "all");
        // props.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
        // props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        
        return props;
    }

    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        // Mock configuration - in production, uncomment the actual Kafka config
        props.put("bootstrap.servers", bootstrapServers);
        props.put("schema.registry.url", schemaRegistryUrl);
        
        // These would be the actual consumer configs:
        // props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        // props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        // props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        // props.put("schema.registry.url", schemaRegistryUrl);
        // props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
        // props.put(ConsumerConfig.GROUP_ID_CONFIG, "platform-service-group");
        // props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        
        return props;
    }
}
