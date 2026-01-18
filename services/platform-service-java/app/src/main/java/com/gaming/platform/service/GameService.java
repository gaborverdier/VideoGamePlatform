package com.gaming.platform.service;

import com.gaming.platform.model.Game;
import com.gaming.platform.repository.GameRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service gérant les opérations sur les jeux.
 *
 * RESPONSABILITÉS :
 * 1. Fournir les opérations CRUD sur les jeux
 * 2. Recherche et filtrage de jeux
 * 3. Synchronisation avec les événements du Publisher Service
 */
@Service
public class GameService {

    private static final Logger logger = LoggerFactory.getLogger(GameService.class);

    private final GameRepository gameRepository;

    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    /**
     * Récupère tous les jeux du catalogue.
     *
     * @return Liste de tous les jeux
     */
    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }

    /**
     * Récupère un jeu par son ID.
     *
     * @param gameId L'identifiant du jeu
     * @return Optional contenant le jeu si trouvé
     */
    public Optional<Game> getGameById(String gameId) {
        return gameRepository.findById(gameId);
    }

    /**
     * Recherche des jeux par titre (recherche partielle, insensible à la casse).
     *
     * @param title Le titre (ou partie du titre) à rechercher
     * @return Liste des jeux correspondants
     */
    public List<Game> searchGamesByTitle(String title) {
        return gameRepository.findByTitleContainingIgnoreCase(title);
    }

    /**
     * Récupère les jeux d'un éditeur.
     *
     * @param publisher Le nom de l'éditeur
     * @return Liste des jeux de cet éditeur
     */
    public List<Game> getGamesByPublisher(String publisher) {
        return gameRepository.findByPublisher(publisher);
    }

    /**
     * Récupère les jeux d'un genre.
     *
     * @param genre Le genre
     * @return Liste des jeux de ce genre
     */
    public List<Game> getGamesByGenre(String genre) {
        return gameRepository.findByGenre(genre);
    }

    /**
     * Récupère les jeux d'une plateforme.
     *
     * @param platform La plateforme
     * @return Liste des jeux de cette plateforme
     */
    public List<Game> getGamesByPlatform(String platform) {
        return gameRepository.findByPlatform(platform);
    }

    /**
     * Crée ou met à jour un jeu.
     *
     * @param game Le jeu à sauvegarder
     * @return Le jeu sauvegardé
     */
    @Transactional
    public Game saveGame(Game game) {
        logger.info("Sauvegarde du jeu: {}", game.getTitle());
        return gameRepository.save(game);
    }

    /**
     * Compte le nombre total de jeux.
     *
     * @return Le nombre de jeux
     */
    public long countGames() {
        return gameRepository.count();
    }

    /**
     * Supprime un jeu.
     *
     * @param gameId L'identifiant du jeu à supprimer
     */
    @Transactional
    public void deleteGame(String gameId) {
        logger.info("Suppression du jeu: {}", gameId);
        gameRepository.deleteById(gameId);
    }
}
