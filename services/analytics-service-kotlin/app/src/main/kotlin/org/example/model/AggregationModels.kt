package org.example.model

/**
 * Internal aggregation state for tracking crash statistics
 */
data class CrashAggregationState(
    val crashCount: Long = 0,
    val uniqueUsers: MutableSet<String> = mutableSetOf(),
    val platforms: MutableSet<String> = mutableSetOf(),
    val errorMessages: MutableMap<String, Int> = mutableMapOf()
) {
    fun addCrash(userId: String, platform: String, errorMessage: String?): CrashAggregationState {
        return copy(
            crashCount = crashCount + 1,
            uniqueUsers = uniqueUsers.apply { add(userId) },
            platforms = platforms.apply { add(platform) },
            errorMessages = errorMessages.apply {
                errorMessage?.let { 
                    put(it, getOrDefault(it, 0) + 1)
                }
            }
        )
    }
    
    fun getMostCommonError(): String? {
        return errorMessages.maxByOrNull { it.value }?.key
    }
}

/**
 * Internal aggregation state for tracking game popularity
 */
data class PopularityAggregationState(
    val sessionCount: Long = 0,
    val crashCount: Long = 0
) {
    fun addSession(): PopularityAggregationState {
        return copy(sessionCount = sessionCount + 1)
    }
    
    fun addCrash(): PopularityAggregationState {
        return copy(crashCount = crashCount + 1)
    }
    
    fun calculatePopularityScore(): Double {
        if (sessionCount == 0L) return 0.0
        // Score = sessions - (crashes * 10) to penalize crashes heavily
        // Normalized between 0 and 100
        val rawScore = sessionCount - (crashCount * 10)
        return maxOf(0.0, minOf(100.0, rawScore.toDouble()))
    }
    
    fun getQualityRating(): String {
        if (sessionCount == 0L) return "NO_DATA"
        val crashRate = crashCount.toDouble() / sessionCount.toDouble()
        return when {
            crashRate <= 0.01 -> "EXCELLENT"  // < 1% crash rate
            crashRate <= 0.05 -> "GOOD"       // < 5% crash rate
            crashRate <= 0.10 -> "AVERAGE"    // < 10% crash rate
            crashRate <= 0.20 -> "POOR"       // < 20% crash rate
            else -> "CRITICAL"                // > 20% crash rate
        }
    }
}
