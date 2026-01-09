package com.gaming.publisher.repository;

import com.gaming.publisher.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour l'accès aux données des jeux.
 *
 * Spring Data JPA génère automatiquement l'implémentation
 * des méthodes CRUD et des requêtes personnalisées.
 */
@Repository
public interface GameRepository extends JpaRepository<Game, String> {

    /**
     * Recherche un jeu par son titre exact.
     *
     * @param title Titre du jeu
     * @return Optional contenant le jeu si trouvé
     */
    Optional<Game> findByTitle(String title);

    /**
     * Recherche tous les jeux d'un éditeur donné.
     *
     * @param publisher Nom de l'éditeur
     * @return Liste des jeux de cet éditeur
     */
    List<Game> findByPublisher(String publisher);

    /**
     * Recherche tous les jeux d'un genre donné.
     *
     * @param genre Genre du jeu
     * @return Liste des jeux de ce genre
     */
    List<Game> findByGenre(String genre);

    /**
     * Recherche tous les jeux d'une plateforme donnée.
     *
     * @param platform Nom de la plateforme
     * @return Liste des jeux de cette plateforme
     */
    List<Game> findByPlatform(String platform);

    /**
     * Recherche des jeux dont le titre contient une chaîne (insensible à la casse).
     *
     * @param title Fragment du titre
     * @return Liste des jeux correspondants
     */
    List<Game> findByTitleContainingIgnoreCase(String title);

    /**
     * Compte le nombre de jeux d'un éditeur.
     *
     * @param publisher Nom de l'éditeur
     * @return Nombre de jeux
     */
    long countByPublisher(String publisher);

    /**
     * Vérifie si un jeu existe par son titre (pour éviter les doublons lors de l'import).
     *
     * @param title Titre du jeu
     * @return true si le jeu existe
     */
    boolean existsByTitle(String title);

    /**
     * Recherche un jeu aléatoire (utile pour la simulation).
     *
     * @return Un jeu aléatoire ou null si la table est vide
     */
    @Query(value = "SELECT * FROM games ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Optional<Game> findRandomGame();
}



