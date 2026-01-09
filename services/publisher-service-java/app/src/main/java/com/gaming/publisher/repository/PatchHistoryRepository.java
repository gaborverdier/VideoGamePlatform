package com.gaming.publisher.repository;

import com.gaming.publisher.model.PatchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour l'accès aux données de l'historique des patches.
 */
@Repository
public interface PatchHistoryRepository extends JpaRepository<PatchHistory, String> {

    /**
     * Récupère tous les patches d'un jeu.
     *
     * @param gameId Identifiant du jeu
     * @return Liste des patches triés par date (plus récents en premier)
     */
    List<PatchHistory> findByGameIdOrderByReleaseDateDesc(String gameId);

    /**
     * Récupère le dernier patch publié pour un jeu.
     *
     * @param gameId Identifiant du jeu
     * @return Optional du dernier patch
     */
    Optional<PatchHistory> findFirstByGameIdOrderByReleaseDateDesc(String gameId);

    /**
     * Compte le nombre de patches publiés pour un jeu.
     *
     * @param gameId Identifiant du jeu
     * @return Nombre de patches
     */
    long countByGameId(String gameId);

    /**
     * Vérifie si une version de patch existe déjà.
     *
     * @param gameId Identifiant du jeu
     * @param version Version du patch
     * @return true si la version existe
     */
    boolean existsByGameIdAndVersion(String gameId, String version);
}

