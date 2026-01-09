package com.gaming.publisher.service;

import com.gaming.publisher.dto.GamePatchedEvent;
import com.gaming.publisher.model.Game;
import com.gaming.publisher.model.PatchHistory;
import com.gaming.publisher.producer.GamePatchedProducer;
import com.gaming.publisher.repository.GameRepository;
import com.gaming.publisher.repository.PatchHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * Service gérant la publication de patches pour les jeux.
 *
 * RESPONSABILITÉS :
 * 1. Créer et déployer des patches
 * 2. Mettre à jour la version des jeux en base
 * 3. Enregistrer l'historique des patches
 * 4. Publier les événements Kafka
 *
 * PATTERN : Service Layer (sépare la logique métier des controllers)
 */
@Service
public class PatchService {

    private static final Logger logger = LoggerFactory.getLogger(PatchService.class);
    private static final Random random = new Random();

    private final GameRepository gameRepository;
    private final PatchHistoryRepository patchHistoryRepository;
    private final GamePatchedProducer patchProducer;
    private final String publisherName;

    /**
     * Constructeur avec injection de dépendances.
     */
    public PatchService(
            GameRepository gameRepository,
            PatchHistoryRepository patchHistoryRepository,
            GamePatchedProducer patchProducer,
            @Value("${publisher.name}") String publisherName) {

        this.gameRepository = gameRepository;
        this.patchHistoryRepository = patchHistoryRepository;
        this.patchProducer = patchProducer;
        this.publisherName = publisherName;
    }

    /**
     * Déploie un patch pour un jeu.
     *
     * TRANSACTIONNEL : Toutes les opérations en base sont atomiques.
     * Si une erreur survient, tout est rollback.
     *
     * WORKFLOW :
     * 1. Récupère le jeu en base
     * 2. Calcule la nouvelle version
     * 3. Met à jour le jeu
     * 4. Crée l'entrée d'historique
     * 5. Publie l'événement Kafka
     *
     * @param gameId ID du jeu
     * @param changelog Description des changements
     * @return Le patch créé
     * @throws IllegalArgumentException si le jeu n'existe pas
     */
    @Transactional
    public PatchHistory deployPatch(String gameId, String changelog) {
        logger.info("Déploiement d'un patch pour le jeu: {}", gameId);

        // 1. Récupère le jeu
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Jeu introuvable: " + gameId));

        // 2. Calcule la nouvelle version
        String previousVersion = game.getCurrentVersion();
        String newVersion = incrementVersion(previousVersion);

        // 3. Met à jour le jeu
        game.setCurrentVersion(newVersion);
        gameRepository.save(game);

        // 4. Crée l'historique
        PatchHistory patch = PatchHistory.builder()
            .gameId(gameId)
            .gameTitle(game.getTitle())
            .version(newVersion)
            .previousVersion(previousVersion)
            .changelog(changelog)
            .patchSize(generateRandomPatchSize())
            .releaseDate(LocalDateTime.now())
            .build();

        patchHistoryRepository.save(patch);

        // 5. Publie l'événement Kafka
        publishPatchEvent(game, patch);

        logger.info("Patch {} -> {} déployé avec succès pour '{}'",
            previousVersion, newVersion, game.getTitle());

        return patch;
    }

    /**
     * Incrémente la version d'un jeu (semantic versioning).
     *
     * RÈGLE : Incrémente le PATCH (dernier chiffre)
     * Exemple : 1.2.3 -> 1.2.4
     *
     * @param currentVersion Version actuelle (format: MAJOR.MINOR.PATCH)
     * @return Nouvelle version
     */
    private String incrementVersion(String currentVersion) {
        try {
            String[] parts = currentVersion.split("\\.");
            if (parts.length == 3) {
                int major = Integer.parseInt(parts[0]);
                int minor = Integer.parseInt(parts[1]);
                int patch = Integer.parseInt(parts[2]);

                // Incrémente le patch
                patch++;

                return String.format("%d.%d.%d", major, minor, patch);
            }
        } catch (Exception e) {
            logger.warn("Format de version invalide: {}, utilisation par défaut", currentVersion);
        }

        // Fallback
        return "1.0.1";
    }

    /**
     * Génère une taille de patch aléatoire (pour simulation).
     *
     * @return Taille en octets (entre 10 MB et 500 MB)
     */
    private long generateRandomPatchSize() {
        // Entre 10 MB et 500 MB
        return (long) (10_000_000 + random.nextDouble() * 490_000_000);
    }

    /**
     * Publie un événement de patch sur Kafka.
     *
     * @param game Jeu concerné
     * @param patch Patch créé
     */
    private void publishPatchEvent(Game game, PatchHistory patch) {
        GamePatchedEvent event = GamePatchedEvent.builder()
            .gameId(game.getId())
            .gameTitle(game.getTitle())
            .version(patch.getVersion())
            .previousVersion(patch.getPreviousVersion())
            .changelog(patch.getChangelog())
            .patchSize(patch.getPatchSize())
            .releaseTimestamp(System.currentTimeMillis())
            .publisher(publisherName)
            .build();

        patchProducer.publishPatch(event);
    }

    /**
     * Génère un changelog automatique (pour simulation).
     *
     * @return Changelog généré
     */
    public String generateRandomChangelog() {
        String[] fixes = {
            "Fixed memory leak in multiplayer mode",
            "Improved graphics rendering performance",
            "Corrected collision detection issues",
            "Fixed crash on level transition",
            "Optimized loading times",
            "Resolved audio sync problems",
            "Fixed UI scaling on 4K displays",
            "Corrected save game corruption bug"
        };

        String[] improvements = {
            "Enhanced AI behavior",
            "Improved netcode stability",
            "Better frame rate consistency",
            "Reduced stuttering",
            "Optimized texture loading"
        };

        // Sélectionne 2-3 fixes et 1-2 améliorations
        StringBuilder changelog = new StringBuilder();
        changelog.append("## Bug Fixes\n");

        int numFixes = 2 + random.nextInt(2); // 2 ou 3
        for (int i = 0; i < numFixes; i++) {
            changelog.append("- ").append(fixes[random.nextInt(fixes.length)]).append("\n");
        }

        changelog.append("\n## Improvements\n");
        int numImprovements = 1 + random.nextInt(2); // 1 ou 2
        for (int i = 0; i < numImprovements; i++) {
            changelog.append("- ").append(improvements[random.nextInt(improvements.length)]).append("\n");
        }

        return changelog.toString();
    }
}

