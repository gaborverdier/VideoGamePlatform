package org.example.topology

import com.gaming.api.models.CrashAggregationModel
import com.gaming.events.GameCrashReported
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.kstream.*
import org.example.config.KafkaStreamsConfig
import java.time.Duration

/**
 * Topology for aggregating game crash events
 * Input: game-crash-reported
 * Output: crash-aggregated
 * Window: Tumbling 1 minutes
 */
class CrashAggregationTopology {
    
    companion object {
        private const val INPUT_TOPIC = "game-crash-reported"
        private const val OUTPUT_TOPIC = "crash-aggregated"
        private val WINDOW_SIZE = Duration.ofMinutes(1)
    }
    
    fun build(builder: StreamsBuilder) {
        // Serdes configuration
        val crashSerde: SpecificAvroSerde<GameCrashReported> = KafkaStreamsConfig.createAvroSerde()
        val crashAggregatedSerde: SpecificAvroSerde<CrashAggregationModel> = KafkaStreamsConfig.createAvroSerde()
        
        // 1. Read crash events from Kafka
        val crashStream: KStream<String, GameCrashReported> = builder.stream(
            INPUT_TOPIC,
            Consumed.with(Serdes.String(), crashSerde)
                // TEMPORARY FIX: Use Kafka message timestamp instead of crashTimestamp
                // because crash events have incorrect timestamps (1970 instead of 2026)
                .withTimestampExtractor { record, _ ->
                    record.timestamp() // Use when message arrived in Kafka
                }
        )
            // Log incoming crashes for debugging
            .peek { key, crash ->
                println("🔴 Crash received: gameId=${crash.getGameId()}, crashTimestamp=${java.time.Instant.ofEpochMilli(crash.getCrashTimestamp())} (INCORRECT - using Kafka timestamp instead)")
            }
        
        // 2. Group by gameId and apply tumbling window
        val crashCounts: KTable<Windowed<String>, Long> = crashStream
            // Extract gameId as key
            .selectKey { _, crash -> crash.getGameId().toString() }
            
            // Group by gameId
            .groupByKey(Grouped.with(Serdes.String(), crashSerde))
            
            // Apply tumbling window of 10 seconds with grace period
            // Grace period of 1 second allows late events but forces window to close
            .windowedBy(TimeWindows.ofSizeAndGrace(WINDOW_SIZE, Duration.ofSeconds(1)))
            
            // Count crashes per window
            .count()
        
        // 3. Transform to CrashAggregationModel
        // Note: Without suppress(), this will emit on every update (every crash)
        // The publisher will receive multiple updates per window as crashes occur
        val crashAggregation: KStream<String, CrashAggregationModel> = crashCounts
            .toStream()
            .map { windowedKey, count ->
                val gameId = windowedKey.key()
                val window = windowedKey.window()
                
                val aggregation = CrashAggregationModel.newBuilder()
                    .setId("${gameId}-${window.start()}")
                    .setGameId(gameId)
                    .setCrashCount(count)
                    .setTimestamp(System.currentTimeMillis())
                    .setWindowStart(window.start())
                    .setWindowEnd(window.end())
                    .build()
                
                KeyValue(gameId, aggregation)
            }
            // Log for debugging before writing
            .peek { key, value ->
                val windowStart = java.time.Instant.ofEpochMilli(value.getWindowStart())
                val windowEnd = java.time.Instant.ofEpochMilli(value.getWindowEnd())
                println("📊 Crash Aggregation produced: gameId=$key, count=${value.getCrashCount()}")
                println("   Window: [$windowStart - $windowEnd]")
            }
        
        // 4. Write to output topic
        crashAggregation.to(
            OUTPUT_TOPIC,
            Produced.with(Serdes.String(), crashAggregatedSerde)
        )
        
        println("✅ Crash Aggregation Topology built")
        println("   📥 Input: $INPUT_TOPIC")
        println("   📤 Output: $OUTPUT_TOPIC")
        println("   ⏱️  Window: ${WINDOW_SIZE.seconds} seconds (Tumbling)")
        println("   ⚠️  Window emitted when next event arrives after window end")
    }
}
