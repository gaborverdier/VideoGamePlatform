package com.gaming.platform.repository;

import com.gaming.platform.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository JPA pour l'entité Game.
 *
 * Fournit les opérations CRUD de base et des méthodes de recherche personnalisées.
 */
@Repository
public interface GameRepository extends JpaRepository<Game, String> {

    /**
     * Recherche des jeux par titre (insensible à la casse, recherche partielle).
     *
     * @param title Le titre (ou partie du titre) à rechercher
     * @return Liste des jeux correspondants
     */
    List<Game> findByTitleContainingIgnoreCase(String title);

    /**
     * Recherche des jeux par éditeur.
     *
     * @param publisher Le nom de l'éditeur
     * @return Liste des jeux de cet éditeur
     */
    List<Game> findByPublisher(String publisher);

    /**
     * Recherche des jeux par genre.
     *
     * @param genre Le genre à rechercher
     * @return Liste des jeux de ce genre
     */
    List<Game> findByGenre(String genre);

    /**
     * Recherche des jeux par plateforme.
     *
     * @param platform La plateforme à rechercher
     * @return Liste des jeux sur cette plateforme
     */
    List<Game> findByPlatform(String platform);
}
