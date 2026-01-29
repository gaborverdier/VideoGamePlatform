package org.example

import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.example.config.KafkaStreamsConfig
import org.example.topology.CrashAggregationTopology
import org.example.topology.PopularityScoreTopology
import java.util.concurrent.CountDownLatch

class App {
    val greeting: String
        get() {
            return """
#################################
   Analytics Service Started!
   🎯 Kafka Streams Aggregator
#################################
            """.trimIndent()
        }
}

fun main() {
    println(App().greeting)
    
    // Build Kafka Streams Topology
    val builder = StreamsBuilder()
    
    // Add Crash Aggregation Topology
    CrashAggregationTopology().build(builder)
    
    // Add Popularity Score Topology
    PopularityScoreTopology().build(builder)
    
    // Build the Kafka Streams application
    val topology = builder.build()
    println("\n📊 Topology Description:")
    println(topology.describe())
    
    // Create and start Kafka Streams
    val streams = KafkaStreams(topology, KafkaStreamsConfig.getStreamsProperties())
    
    // Handle graceful shutdown
    val latch = CountDownLatch(1)
    Runtime.getRuntime().addShutdownHook(Thread {
        println("\n🛑 Shutting down Analytics Service...")
        streams.close()
        latch.countDown()
    })
    
    try {
        println("\n🚀 Starting Kafka Streams processing...")
        streams.start()
        latch.await()
    } catch (e: Exception) {
        println("❌ Error: ${e.message}")
        e.printStackTrace()
        System.exit(1)
    }
    
    println("✅ Analytics Service stopped")
    System.exit(0)
}
