package com.gaming.publisher.repository;

import com.gaming.publisher.model.CrashReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository pour l'accès aux données des rapports de crash.
 */
@Repository
public interface CrashReportRepository extends JpaRepository<CrashReport, String> {

    /**
     * Récupère tous les crashs d'un jeu donné.
     *
     * @param gameId Identifiant du jeu
     * @return Liste des crashs
     */
    List<CrashReport> findByGameId(String gameId);

    /**
     * Récupère les crashs d'un jeu triés par date (plus récents en premier).
     *
     * @param gameId Identifiant du jeu
     * @return Liste des crashs triés
     */
    List<CrashReport> findByGameIdOrderByCrashTimestampDesc(String gameId);

    /**
     * Compte le nombre de crashs pour un jeu.
     *
     * @param gameId Identifiant du jeu
     * @return Nombre de crashs
     */
    long countByGameId(String gameId);

    /**
     * Compte le nombre de crashs pour un jeu dans une plage de temps.
     *
     * @param gameId Identifiant du jeu
     * @param start Date de début
     * @param end Date de fin
     * @return Nombre de crashs
     */
    long countByGameIdAndCrashTimestampBetween(String gameId, LocalDateTime start, LocalDateTime end);

    /**
     * Récupère les crashs par code d'erreur.
     *
     * @param errorCode Code d'erreur
     * @return Liste des crashs avec ce code
     */
    List<CrashReport> findByErrorCode(String errorCode);

    /**
     * Récupère les jeux avec le plus de crashs (pour alerting).
     *
     * @param threshold Seuil minimum de crashs
     * @return Liste des paires [gameId, count]
     */
    @Query("SELECT c.gameId, COUNT(c) FROM CrashReport c GROUP BY c.gameId HAVING COUNT(c) > ?1")
    List<Object[]> findGamesWithCrashesAboveThreshold(long threshold);

    /**
     * Récupère les crashs récents (dernières 24h).
     *
     * @param since Date de début
     * @return Liste des crashs récents
     */
    List<CrashReport> findByCrashTimestampAfter(LocalDateTime since);
}

