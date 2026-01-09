package com.gaming.publisher.repository;

import com.gaming.publisher.model.ReviewStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour l'accès aux statistiques de notes des jeux.
 */
@Repository
public interface ReviewStatsRepository extends JpaRepository<ReviewStats, String> {

    /**
     * Récupère toutes les statistiques d'un jeu.
     *
     * @param gameId Identifiant du jeu
     * @return Liste des statistiques triées par date
     */
    List<ReviewStats> findByGameIdOrderByAggregationTimestampDesc(String gameId);

    /**
     * Récupère la statistique la plus récente pour un jeu.
     *
     * @param gameId Identifiant du jeu
     * @return Optional de la statistique la plus récente
     */
    Optional<ReviewStats> findFirstByGameIdOrderByAggregationTimestampDesc(String gameId);

    /**
     * Récupère les jeux avec une note moyenne supérieure à un seuil.
     *
     * @param minRating Note minimale
     * @return Liste des statistiques
     */
    List<ReviewStats> findByAverageRatingGreaterThanEqual(Double minRating);

    /**
     * Récupère les jeux avec une note moyenne inférieure à un seuil (jeux en difficulté).
     *
     * @param maxRating Note maximale
     * @return Liste des statistiques
     */
    List<ReviewStats> findByAverageRatingLessThan(Double maxRating);
}

