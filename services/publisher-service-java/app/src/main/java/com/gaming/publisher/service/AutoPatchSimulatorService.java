package com.gaming.publisher.service;

import com.gaming.publisher.model.Game;
import com.gaming.publisher.repository.GameRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service de simulation automatique de publication de patches.
 *
 * RESPONSABILITÉ : Générer automatiquement du trafic Kafka pour
 * démonstration et tests en publiant des patches périodiquement.
 *
 * UTILISATION :
 * - Activé par défaut
 * - S'exécute toutes les 2 minutes
 * - Sélectionne un jeu aléatoire et publie un patch
 *
 * DÉSACTIVATION : Commenter l'annotation @Scheduled si besoin
 */
@Service
public class AutoPatchSimulatorService {

    private static final Logger logger = LoggerFactory.getLogger(AutoPatchSimulatorService.class);

    private final GameRepository gameRepository;
    private final PatchService patchService;

    public AutoPatchSimulatorService(
            GameRepository gameRepository,
            PatchService patchService) {

        this.gameRepository = gameRepository;
        this.patchService = patchService;
    }

    /**
     * Tâche planifiée qui publie un patch aléatoire.
     *
     * SCHEDULE : S'exécute toutes les 2 minutes (120000 ms)
     *
     * LOGIQUE :
     * 1. Sélectionne un jeu aléatoire en base
     * 2. Génère un changelog aléatoire
     * 3. Déploie le patch
     *
     * Note : Pour modifier la fréquence, changer fixedDelay
     */
    @Scheduled(fixedDelay = 120000, initialDelay = 30000)
    public void simulateRandomPatch() {
        try {
            // Vérifie qu'il y a des jeux en base
            long gameCount = gameRepository.count();
            if (gameCount == 0) {
                logger.debug("Aucun jeu en base, simulation ignorée");
                return;
            }

            // Sélectionne un jeu aléatoire
            Optional<Game> randomGame = gameRepository.findRandomGame();

            if (randomGame.isEmpty()) {
                logger.debug("Impossible de sélectionner un jeu aléatoire");
                return;
            }

            Game game = randomGame.get();

            // Génère un changelog aléatoire
            String changelog = patchService.generateRandomChangelog();

            // Déploie le patch
            logger.info("🤖 [AUTO-SIMULATION] Publication d'un patch pour '{}'", game.getTitle());
            patchService.deployPatch(game.getId(), changelog);

        } catch (Exception e) {
            logger.error("Erreur lors de la simulation de patch: {}", e.getMessage(), e);
        }
    }

    /**
     * Méthode publique pour déclencher manuellement une simulation.
     * Utile pour l'API REST.
     */
    public void triggerManualSimulation() {
        logger.info("Déclenchement manuel de la simulation de patch");
        simulateRandomPatch();
    }
}

