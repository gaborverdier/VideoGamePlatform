package com.gaming.publisher.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO représentant un événement d'agrégation de notes.
 *
 * NOTE IMPORTANTE : Classe temporaire, à remplacer par la version
 * générée depuis game-rating-aggregated.avsc
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameRatingAggregatedEvent {
    private String gameId;
    private String gameTitle;
    private Double averageRating;
    private Long totalRatings;
    private Map<String, Long> ratingDistribution;
    private Long aggregationTimestamp;
    private Long timeWindowStart;
    private Long timeWindowEnd;
}

