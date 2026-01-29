package org.example.topology

import com.gaming.analytics.GameCrashStats
import com.gaming.events.GameCrashReported
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.*
import org.example.config.KafkaStreamsConfig
import java.time.Duration

/**
 * Topology for aggregating game crash events
 * Input: game-crash-reported
 * Output: crash-aggregated
 * Window: Tumbling 5 minutes
 */
class CrashAggregationTopology {
    
    companion object {
        private const val INPUT_TOPIC = "game-crash-reported"
        private const val OUTPUT_TOPIC = "crash-aggregated"
        private val WINDOW_SIZE = Duration.ofMinutes(5)
    }
    
    fun build(builder: StreamsBuilder) {
        val crashSerde: SpecificAvroSerde<GameCrashReported> = KafkaStreamsConfig.createAvroSerde()
        val statsSerde: SpecificAvroSerde<GameCrashStats> = KafkaStreamsConfig.createAvroSerde()
        
        // 1. Read crash events from Kafka
        val crashStream: KStream<String, GameCrashReported> = builder.stream(
            INPUT_TOPIC,
            Consumed.with(Serdes.String(), crashSerde)
        )
        
        // 2. Group by gameId and apply tumbling window
        val crashCounts: KTable<Windowed<String>, Long> = crashStream
            // Extract gameId as key
            .selectKey { _, crash -> crash.gameId.toString() }
            
            // Group by gameId
            .groupByKey(Grouped.with(Serdes.String(), crashSerde))
            
            // Apply tumbling window of 5 minutes
            .windowedBy(TimeWindows.ofSizeWithNoGrace(WINDOW_SIZE))
            
            // Utiliser count() qui est optimisé pour les agrégations simples
            .count()
        
        // 3. Transformer en GameCrashStats
        val crashStats: KTable<Windowed<String>, GameCrashStats> = crashCounts
            .mapValues { windowedKey, count ->
                GameCrashStats.newBuilder()
                    .setGameId(windowedKey.key())
                    .setWindowStart(windowedKey.window().start())
                    .setWindowEnd(windowedKey.window().end())
                    .setCrashCount(count)
                    .setUniqueUsersAffected(count) // Approximation : 1 crash = 1 user
                    .setPlatforms(emptyList()) // Simplifié pour éviter complexité
                    .setMostCommonError(null) // Simplifié
                    .build()
            }
        
        // 4. Write to output topic
        crashStats
            .toStream()
            .selectKey { windowedKey, _ -> windowedKey.key() }
            .to(OUTPUT_TOPIC, Produced.with(Serdes.String(), statsSerde))
        
        println("✅ Crash Aggregation Topology built")
        println("   📥 Input: $INPUT_TOPIC")
        println("   📤 Output: $OUTPUT_TOPIC")
        println("   ⏱️  Window: ${WINDOW_SIZE.toMinutes()} minutes (Tumbling)")
    }
}
