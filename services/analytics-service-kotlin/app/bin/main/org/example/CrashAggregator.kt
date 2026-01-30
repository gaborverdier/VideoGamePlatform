package org.example

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import java.time.Duration
import java.util.Properties
import com.gaming.events.GameCrashReported
import org.apache.avro.generic.GenericRecord
import io.confluent.kafka.serializers.KafkaAvroDeserializer

fun main() {
    println("Starting Crash Aggregation Consumer...")
    val props = Properties().apply {
        put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
        put(ConsumerConfig.GROUP_ID_CONFIG, "analytics-crash-aggregator")
        put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
        put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer::class.java.name)
        put("schema.registry.url", "http://localhost:8081")
        put("specific.avro.reader", false)
        put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    }
    val consumer = KafkaConsumer<String, GenericRecord>(props)
    consumer.subscribe(listOf("game-crash-reported"))

    val crashCounts = mutableMapOf<String, Int>() // gameId -> count

    while (true) {
        val records = consumer.poll(Duration.ofMillis(1000))
        for (record in records) {
            val value = record.value()
            val gameId = value.get("gameId")?.toString() ?: continue
            crashCounts[gameId] = crashCounts.getOrDefault(gameId, 0) + 1
            println("Crash for game $gameId. Total: ${crashCounts[gameId]}")
        }
    }
}
