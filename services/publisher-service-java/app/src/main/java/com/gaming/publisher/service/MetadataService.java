package com.gaming.publisher.service;

import com.gaming.publisher.dto.GameMetadataUpdatedEvent;
import com.gaming.publisher.model.Game;
import com.gaming.publisher.producer.GameMetadataProducer;
import com.gaming.publisher.repository.GameRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service gérant la mise à jour des métadonnées des jeux.
 *
 * RESPONSABILITÉS :
 * 1. Mettre à jour les informations d'un jeu (titre, genre, description, etc.)
 * 2. Publier les événements de mise à jour sur Kafka
 */
@Service
public class MetadataService {

    private static final Logger logger = LoggerFactory.getLogger(MetadataService.class);

    private final GameRepository gameRepository;
    private final GameMetadataProducer metadataProducer;
    private final String publisherName;

    public MetadataService(
            GameRepository gameRepository,
            GameMetadataProducer metadataProducer,
            @Value("${publisher.name}") String publisherName) {

        this.gameRepository = gameRepository;
        this.metadataProducer = metadataProducer;
        this.publisherName = publisherName;
    }

    /**
     * Met à jour les métadonnées d'un jeu.
     *
     * WORKFLOW :
     * 1. Récupère le jeu en base
     * 2. Met à jour les champs modifiés (null = pas de changement)
     * 3. Sauvegarde
     * 4. Publie l'événement Kafka
     *
     * @param gameId ID du jeu
     * @param genre Nouveau genre (null pour ignorer)
     * @param platform Nouvelle plateforme (null pour ignorer)
     * @param description Nouvelle description (null pour ignorer)
     * @return Le jeu mis à jour
     * @throws IllegalArgumentException si le jeu n'existe pas
     */
    @Transactional
    public Game updateMetadata(String gameId, String genre, String platform, String description) {
        logger.info("Mise à jour des métadonnées du jeu: {}", gameId);

        // Récupère le jeu
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Jeu introuvable: " + gameId));

        // Met à jour uniquement les champs non-null
        boolean updated = false;

        if (genre != null && !genre.equals(game.getGenre())) {
            game.setGenre(genre);
            updated = true;
        }

        if (platform != null && !platform.equals(game.getPlatform())) {
            game.setPlatform(platform);
            updated = true;
        }

        if (description != null && !description.equals(game.getDescription())) {
            game.setDescription(description);
            updated = true;
        }

        if (!updated) {
            logger.info("Aucune modification détectée pour le jeu: {}", gameId);
            return game;
        }

        // Sauvegarde
        game = gameRepository.save(game);

        // Publie l'événement Kafka
        publishMetadataEvent(game);

        logger.info("Métadonnées mises à jour pour '{}'", game.getTitle());

        return game;
    }

    /**
     * Publie un événement de mise à jour de métadonnées sur Kafka.
     *
     * @param game Jeu mis à jour
     */
    private void publishMetadataEvent(Game game) {
        GameMetadataUpdatedEvent event = GameMetadataUpdatedEvent.builder()
            .gameId(game.getId())
            .gameTitle(game.getTitle())
            .genre(game.getGenre())
            .platform(game.getPlatform())
            .description(game.getDescription())
            .updateTimestamp(System.currentTimeMillis())
            .publisher(publisherName)
            .build();

        metadataProducer.publishMetadataUpdate(event);
    }
}

