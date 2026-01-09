package com.gaming.publisher.consumer;

import com.gaming.publisher.dto.GameRatingAggregatedEvent;
import com.gaming.publisher.model.ReviewStats;
import com.gaming.publisher.repository.ReviewStatsRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Consommateur Kafka pour les événements d'agrégation de notes.
 *
 * RESPONSABILITÉ : Écouter le topic "game-rating-aggregated" et stocker
 * les statistiques de qualité pour suivre la perception des jeux.
 *
 * FONCTIONNEMENT :
 * - Reçoit des agrégats de notes calculés par le service Analytics
 * - Stocke ces stats en base pour historique
 * - Log les tendances qualité (bon/moyen/mauvais)
 *
 * NOTE: Temporairement désactivé - décommenter @Component pour activer
 */
// @Component  // TODO: Décommenter quand Kafka est configuré
public class GameRatingConsumer {

    private static final Logger logger = LoggerFactory.getLogger(GameRatingConsumer.class);

    private final KafkaConsumer<String, GameRatingAggregatedEvent> consumer;
    private final ReviewStatsRepository reviewStatsRepository;
    private final ExecutorService executorService;
    private final String topicName;

    private volatile boolean running = true;

    public GameRatingConsumer(
            @Qualifier("ratingConsumerConfigs") Map<String, Object> ratingConsumerConfigs,
            ReviewStatsRepository reviewStatsRepository,
            @Value("${kafka.topic.game-rating-aggregated}") String topicName) {

        this.consumer = new KafkaConsumer<>(ratingConsumerConfigs);
        this.reviewStatsRepository = reviewStatsRepository;
        this.topicName = topicName;
        this.executorService = Executors.newSingleThreadExecutor();

        logger.info("GameRatingConsumer initialisé pour le topic: {}", topicName);
    }

    @PostConstruct
    public void start() {
        consumer.subscribe(Collections.singletonList(topicName));
        executorService.submit(this::consume);
        logger.info("GameRatingConsumer démarré et en écoute sur: {}", topicName);
    }

    /**
     * Boucle principale de consommation.
     */
    private void consume() {
        try {
            while (running) {
                ConsumerRecords<String, GameRatingAggregatedEvent> records =
                    consumer.poll(Duration.ofMillis(100));

                for (ConsumerRecord<String, GameRatingAggregatedEvent> record : records) {
                    handleRatingStats(record.value());
                }
            }
        } catch (Exception e) {
            logger.error("Erreur dans la boucle de consommation: {}", e.getMessage(), e);
        } finally {
            consumer.close();
            logger.info("GameRatingConsumer arrêté");
        }
    }

    /**
     * Traite les statistiques de notes reçues.
     *
     * LOGIQUE MÉTIER :
     * 1. Convertit l'événement en entité JPA
     * 2. Sauvegarde en base
     * 3. Log la tendance qualité
     *
     * @param event Événement d'agrégation reçu
     */
    private void handleRatingStats(GameRatingAggregatedEvent event) {
        try {
            logger.info("Stats de notes reçues pour {} - Moyenne: {}/5 ({} votes)",
                event.getGameTitle(),
                String.format("%.2f", event.getAverageRating()),
                event.getTotalRatings());

            // Convertit en entité JPA
            ReviewStats stats = ReviewStats.builder()
                .gameId(event.getGameId())
                .gameTitle(event.getGameTitle())
                .averageRating(event.getAverageRating())
                .totalRatings(event.getTotalRatings())
                .aggregationTimestamp(timestampToLocalDateTime(event.getAggregationTimestamp()))
                .timeWindowStart(timestampToLocalDateTime(event.getTimeWindowStart()))
                .timeWindowEnd(timestampToLocalDateTime(event.getTimeWindowEnd()))
                .build();

            // Sauvegarde en base
            reviewStatsRepository.save(stats);

            // Analyse de la tendance
            analyzeTrend(event.getGameTitle(), event.getAverageRating());

        } catch (Exception e) {
            logger.error("Erreur lors du traitement des stats pour {}: {}",
                event.getGameId(), e.getMessage(), e);
        }
    }

    /**
     * Analyse et log la tendance qualité d'un jeu.
     *
     * BUSINESS LOGIC :
     * - < 2.0 : Très mauvais (action urgente)
     * - 2.0-3.0 : Moyen (attention)
     * - 3.0-4.0 : Bon
     * - >= 4.0 : Excellent
     *
     * @param gameTitle Titre du jeu
     * @param rating Note moyenne
     */
    private void analyzeTrend(String gameTitle, double rating) {
        if (rating >= 4.0) {
            logger.info("✅ Excellent ! '{}' a une note de {}/5", gameTitle, rating);
        } else if (rating >= 3.0) {
            logger.info("👍 Bon. '{}' a une note de {}/5", gameTitle, rating);
        } else if (rating >= 2.0) {
            logger.warn("⚠️ Attention ! '{}' a une note moyenne de {}/5", gameTitle, rating);
        } else {
            logger.error("❌ CRITIQUE ! '{}' a une très mauvaise note de {}/5", gameTitle, rating);
        }
    }

    private LocalDateTime timestampToLocalDateTime(Long timestamp) {
        if (timestamp == null) {
            return LocalDateTime.now();
        }
        return LocalDateTime.ofInstant(
            Instant.ofEpochMilli(timestamp),
            ZoneId.systemDefault()
        );
    }

    @PreDestroy
    public void stop() {
        logger.info("Arrêt du GameRatingConsumer...");
        running = false;
        executorService.shutdown();
    }
}

