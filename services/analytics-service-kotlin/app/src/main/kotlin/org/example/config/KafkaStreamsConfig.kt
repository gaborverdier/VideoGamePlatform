package org.example.config

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsConfig
import java.util.*

object KafkaStreamsConfig {
    
    private const val BOOTSTRAP_SERVERS = "localhost:9092"
    private const val SCHEMA_REGISTRY_URL = "http://localhost:8081"
    private const val APPLICATION_ID = "analytics-service-aggregator"
    
    fun getStreamsProperties(): Properties {
        return Properties().apply {
            // Core Kafka Streams Configuration
            put(StreamsConfig.APPLICATION_ID_CONFIG, APPLICATION_ID)
            put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS)
            
            // Default Serdes
            put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String()::class.java)
            put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde::class.java)
            
            // Schema Registry
            put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, SCHEMA_REGISTRY_URL)
            
            // Performance & Reliability
            put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000) // Commit every 1 second
            put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 10 * 1024 * 1024L) // 10MB cache
            put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, 2) // 2 processing threads
            
            // Processing Guarantees
            put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE_V2)
            
            // State Store
            put(StreamsConfig.STATE_DIR_CONFIG, "./kafka-streams-state")
        }
    }
    
    fun <T : SpecificRecord> createAvroSerde(): SpecificAvroSerde<T> {
        val serde = SpecificAvroSerde<T>()
        val config = mapOf(
            AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG to SCHEMA_REGISTRY_URL
        )
        serde.configure(config, false) // false = value serde
        return serde
    }
}
