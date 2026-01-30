package org.example.topology

import com.gaming.analytics.GamePopularityScore
import com.gaming.api.models.CrashAggregationModel
import com.gaming.events.GameReviewed
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.*
import org.example.config.KafkaStreamsConfig
import java.time.Duration

/**
 * Topology for calculating game popularity score based on reviews and crashes
 * Input: game-reviewed + crash-aggregated
 * Output: game-popularity-score
 * Window: Tumbling 1 hour
 */
class PopularityScoreTopology {
    
    companion object {
        private const val REVIEW_TOPIC = "game-reviewed"
        private const val CRASH_TOPIC = "crash-aggregated"
        private const val OUTPUT_TOPIC = "game-popularity-score"
        private val WINDOW_SIZE = Duration.ofMinutes(60)
    }
    
    fun build(builder: StreamsBuilder) {
        val reviewSerde: SpecificAvroSerde<GameReviewed> = KafkaStreamsConfig.createAvroSerde()
        val crashAggSerde: SpecificAvroSerde<CrashAggregationModel> = KafkaStreamsConfig.createAvroSerde()
        val scoreSerde: SpecificAvroSerde<GamePopularityScore> = KafkaStreamsConfig.createAvroSerde()
        
        // 1. Read review events and aggregate
        val reviewStream: KStream<String, GameReviewed> = builder.stream(
            REVIEW_TOPIC,
            Consumed.with(Serdes.String(), reviewSerde)
                .withTimestampExtractor { record, _ -> record.timestamp() }
        )
            .peek { _, review ->
                println("⭐ Review received: gameId=${review.getGameId()}, rating=${review.getRating()}/5")
            }
        
        // Aggregate reviews: count + sum ratings per game
        val reviewStats: KTable<Windowed<String>, ReviewStats> = reviewStream
            .selectKey { _, review -> review.getGameId().toString() }
            .groupByKey(Grouped.with(Serdes.String(), reviewSerde))
            .windowedBy(TimeWindows.ofSizeWithNoGrace(WINDOW_SIZE))
            .aggregate(
                { ReviewStats(0L, 0.0) },
                { _, review, stats ->
                    ReviewStats(
                        count = stats.count + 1,
                        totalRating = stats.totalRating + review.getRating()
                    )
                },
                Materialized.with(Serdes.String(), reviewStatsSerde())
            )
            .suppress(Suppressed.untilWindowCloses(Suppressed.BufferConfig.unbounded()))
        
        // 2. Read crash aggregations
        val crashStream: KStream<String, CrashAggregationModel> = builder.stream(
            CRASH_TOPIC,
            Consumed.with(Serdes.String(), crashAggSerde)
        )
            .peek { _, crash ->
                println("💥 Crash aggregated: gameId=${crash.getGameId()}, count=${crash.getCrashCount()}")
            }
        
        // Convert crashes to windowed table
        val crashStats: KTable<Windowed<String>, Long> = crashStream
            .selectKey { _, crash -> crash.getGameId() }
            .groupByKey(Grouped.with(Serdes.String(), crashAggSerde))
            .windowedBy(TimeWindows.ofSizeWithNoGrace(WINDOW_SIZE))
            .aggregate(
                { 0L },
                { _, crash, total -> total + crash.getCrashCount() },
                Materialized.with(Serdes.String(), Serdes.Long())
            )
            .suppress(Suppressed.untilWindowCloses(Suppressed.BufferConfig.unbounded()))
        
        // 3. Left join reviews with crashes to calculate score
        reviewStats
            .leftJoin(
                crashStats,
                { reviewStat, crashCount ->
                    calculateScore(reviewStat, crashCount ?: 0L)
                }
            )
            .toStream()
            .map { windowedKey, score ->
                KeyValue(
                    windowedKey.key(),
                    GamePopularityScore.newBuilder(score)
                        .setGameId(windowedKey.key())
                        .setWindowStart(windowedKey.window().start())
                        .setWindowEnd(windowedKey.window().end())
                        .build()
                )
            }
            .peek { gameId, score ->
                println("📊 Popularity score for gameId=$gameId: score=${score.getPopularityScore()}, avgRating=${score.getAverageRating()}, reviews=${score.getTotalReviews()}, crashes=${score.getTotalCrashes()}")
            }
            .to(OUTPUT_TOPIC, Produced.with(Serdes.String(), scoreSerde))
        
        println("✅ Popularity Score Topology built")
        println("   📥 Inputs: $REVIEW_TOPIC + $CRASH_TOPIC")
        println("   📤 Output: $OUTPUT_TOPIC")
        println("   ⏱️  Window: ${WINDOW_SIZE.toMinutes()}min (Tumbling)")
    }
    
    private fun calculateScore(reviewStat: ReviewStats, crashCount: Long): GamePopularityScore {
        val averageRating = if (reviewStat.count > 0) {
            reviewStat.totalRating / reviewStat.count
        } else {
            0.0
        }
        
        // Popularity formula: (avgRating/5 * 100) - (crash penalty)
        // avgRating normalized to 0-100 scale, then subtract crash impact
        val crashPenalty = crashCount * 5.0  // Each crash reduces score by 5 points
        val baseScore = (averageRating / 5.0) * 100.0  // Convert rating to 0-100
        val popularityScore = (baseScore - crashPenalty).coerceIn(0.0, 100.0)
        
        val crashRate = if (reviewStat.count > 0) {
            (crashCount.toDouble() / reviewStat.count.toDouble()) * 100.0
        } else {
            0.0
        }
        
        return GamePopularityScore.newBuilder()
            .setGameId("")  // Will be set later with window info
            .setPopularityScore(popularityScore)
            .setAverageRating(averageRating)
            .setTotalReviews(reviewStat.count)
            .setCrashRate(crashRate)
            .setTotalCrashes(crashCount)
            .setWindowStart(0L)  // Will be set later
            .setWindowEnd(0L)    // Will be set later
            .setTimestamp(System.currentTimeMillis())
            .build()
    }
    
    // Helper data class for review aggregation
    data class ReviewStats(
        val count: Long,
        val totalRating: Double
    )
    
    // Helper serde for ReviewStats
    private fun reviewStatsSerde(): Serde<ReviewStats> {
        return Serdes.serdeFrom(
            { _, data ->
                val json = """{"count":${data.count},"totalRating":${data.totalRating}}"""
                json.toByteArray()
            },
            { _, bytes ->
                val json = String(bytes)
                val count = json.substringAfter("\"count\":").substringBefore(",").toLong()
                val totalRating = json.substringAfter("\"totalRating\":").substringBefore("}").toDouble()
                ReviewStats(count, totalRating)
            }
        )
    }
}
