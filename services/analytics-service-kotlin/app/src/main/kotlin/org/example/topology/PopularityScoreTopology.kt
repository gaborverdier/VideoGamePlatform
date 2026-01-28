package org.example.topology

import com.gaming.analytics.GamePopularityScore
import com.gaming.events.GameCrashReported
import com.gaming.events.GameSessionStarted
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.*
import org.example.config.KafkaStreamsConfig
import java.time.Duration

/**
 * Topology for calculating game popularity score
 * Input: game-session-started + game-crash-reported
 * Output: game-popularity-score
 * Window: Hopping 1 hour, advance 15 minutes
 */
class PopularityScoreTopology {
    
    companion object {
        private const val SESSION_TOPIC = "game-session-started"
        private const val CRASH_TOPIC = "game-crash-reported"
        private const val OUTPUT_TOPIC = "game-popularity-score"
        private val WINDOW_SIZE = Duration.ofHours(1)
        private val ADVANCE_BY = Duration.ofMinutes(15)
    }
    
    fun build(builder: StreamsBuilder) {
        val sessionSerde: SpecificAvroSerde<GameSessionStarted> = KafkaStreamsConfig.createAvroSerde()
        val crashSerde: SpecificAvroSerde<GameCrashReported> = KafkaStreamsConfig.createAvroSerde()
        val scoreSerde: SpecificAvroSerde<GamePopularityScore> = KafkaStreamsConfig.createAvroSerde()
        
        // 1. Read session events
        val sessionStream: KStream<String, GameSessionStarted> = builder.stream(
            SESSION_TOPIC,
            Consumed.with(Serdes.String(), sessionSerde)
        )
        
        // 2. Read crash events
        val crashStream: KStream<String, GameCrashReported> = builder.stream(
            CRASH_TOPIC,
            Consumed.with(Serdes.String(), crashSerde)
        )
        
        // 3. Aggregate sessions by game (utilise Long natif - pas besoin de serde custom)
        val sessionCounts: KTable<Windowed<String>, Long> = sessionStream
            .selectKey { _, session -> session.gameId.toString() }
            .groupByKey(Grouped.with(Serdes.String(), sessionSerde))
            .windowedBy(
                TimeWindows.ofSizeWithNoGrace(WINDOW_SIZE)
                    .advanceBy(ADVANCE_BY)
            )
            .count()
        
        // 4. Aggregate crashes by game (utilise Long natif)
        val crashCounts: KTable<Windowed<String>, Long> = crashStream
            .selectKey { _, crash -> crash.gameId.toString() }
            .groupByKey(Grouped.with(Serdes.String(), crashSerde))
            .windowedBy(
                TimeWindows.ofSizeWithNoGrace(WINDOW_SIZE)
                    .advanceBy(ADVANCE_BY)
            )
            .count()
        
        // 5. Join session and crash counts et calculer le score
        val popularityScores: KTable<Windowed<String>, GamePopularityScore> = sessionCounts
            .outerJoin(
                crashCounts,
                { sessions, crashes ->
                    val sessionCount = sessions ?: 0L
                    val crashCount = crashes ?: 0L
                    
                    // Calculer le score de popularité
                    val rawScore = sessionCount - (crashCount * 10)
                    val popularityScore = maxOf(0.0, minOf(100.0, rawScore.toDouble()))
                    
                    // Calculer le rating qualité
                    val crashRate = if (sessionCount > 0) crashCount.toDouble() / sessionCount.toDouble() else 0.0
                    val qualityRating = when {
                        sessionCount == 0L -> "NO_DATA"
                        crashRate <= 0.01 -> "EXCELLENT"
                        crashRate <= 0.05 -> "GOOD"
                        crashRate <= 0.10 -> "AVERAGE"
                        crashRate <= 0.20 -> "POOR"
                        else -> "CRITICAL"
                    }
                    
                    // Retourner directement l'objet Avro
                    GamePopularityScore.newBuilder()
                        .setGameId("")  // Sera mis à jour après
                        .setWindowStart(0L)  // Sera mis à jour après
                        .setWindowEnd(0L)    // Sera mis à jour après
                        .setActiveSessionCount(sessionCount)
                        .setCrashCount(crashCount)
                        .setPopularityScore(popularityScore)
                        .setQualityRating(qualityRating)
                        .build()
                }
            )
            .mapValues { windowedKey, score ->
                // Mettre à jour les champs de fenêtre
                GamePopularityScore.newBuilder(score)
                    .setGameId(windowedKey.key())
                    .setWindowStart(windowedKey.window().start())
                    .setWindowEnd(windowedKey.window().end())
                    .build()
            }
        
        // 6. Write to output topic
        popularityScores
            .toStream()
            .selectKey { windowedKey, _ -> windowedKey.key() }
            .to(OUTPUT_TOPIC, Produced.with(Serdes.String(), scoreSerde))
        
        println("✅ Popularity Score Topology built")
        println("   📥 Inputs: $SESSION_TOPIC + $CRASH_TOPIC")
        println("   📤 Output: $OUTPUT_TOPIC")
        println("   ⏱️  Window: ${WINDOW_SIZE.toHours()}h (Hopping, advance ${ADVANCE_BY.toMinutes()}min)")
    }
}
