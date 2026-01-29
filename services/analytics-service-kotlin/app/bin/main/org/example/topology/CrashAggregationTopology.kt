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
 * Window: Tumbling 5 minutes
 */
class CrashAggregationTopology {
    
    companion object {
        private const val INPUT_TOPIC = "game-crash-reported"
        private const val OUTPUT_TOPIC = "crash-aggregated"
        private val WINDOW_SIZE = Duration.ofMinutes(5)
    }
    
    fun build(builder: StreamsBuilder) {
        // Serdes configuration
        val crashSerde: SpecificAvroSerde<GameCrashReported> = KafkaStreamsConfig.createAvroSerde()
        val crashAggregatedSerde: SpecificAvroSerde<CrashAggregationModel> = KafkaStreamsConfig.createAvroSerde()
        
        // 1. Read crash events from Kafka
        val crashStream: KStream<String, GameCrashReported> = builder.stream(
            INPUT_TOPIC,
            Consumed.with(Serdes.String(), crashSerde)
        )
        
        // 2. Group by gameId and apply tumbling window
        val crashCounts: KTable<Windowed<String>, Long> = crashStream
            // Extract gameId as key
            .selectKey { _, crash -> crash.getGameId().toString() }
            
            // Group by gameId
            .groupByKey(Grouped.with(Serdes.String(), crashSerde))
            
            // Apply tumbling window of 5 minutes
            .windowedBy(TimeWindows.ofSizeWithNoGrace(WINDOW_SIZE))
            
            // Count crashes per window
            .count()
        
        // 3. Transform to CrashAggregationModel
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
        
        // 4. Write to output topic
        crashAggregation.to(
            OUTPUT_TOPIC,
            Produced.with(Serdes.String(), crashAggregatedSerde)
        )
        
        // 5. Log for debugging
        crashAggregation.foreach { key, value ->
            println("📊 Crash Aggregation produced: gameId=$key, count=${value.getCrashCount()}, window=[${value.getWindowStart()} - ${value.getWindowEnd()}]")
        }
        
        println("✅ Crash Aggregation Topology built")
        println("   📥 Input: $INPUT_TOPIC")
        println("   📤 Output: $OUTPUT_TOPIC")
        println("   ⏱️  Window: ${WINDOW_SIZE.toMinutes()} minutes (Tumbling)")
    }
}
