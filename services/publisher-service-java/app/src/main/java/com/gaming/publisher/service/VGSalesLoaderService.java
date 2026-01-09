package com.gaming.publisher.service;

import com.gaming.publisher.model.Game;
import com.gaming.publisher.repository.GameRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service chargé d'importer les jeux depuis le fichier VGSales CSV.
 *
 * RESPONSABILITÉS :
 * 1. Lire le fichier vgsales.csv au démarrage
 * 2. Parser les données et créer les entités Game
 * 3. Sauvegarder en base de données
 *
 * LIFECYCLE : Implémente CommandLineRunner pour s'exécuter au démarrage
 *
 * FORMAT CSV ATTENDU :
 * Name, Platform, Year, Genre, Publisher, NA_Sales, EU_Sales, JP_Sales, Other_Sales, Global_Sales
 */
@Service
public class VGSalesLoaderService implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(VGSalesLoaderService.class);

    private final GameRepository gameRepository;
    private final String vgsalesPath;
    private final boolean autoLoad;
    private final String publisherFilter;

    /**
     * Constructeur avec injection de configuration.
     *
     * @param gameRepository Repository pour sauvegarder les jeux
     * @param vgsalesPath Chemin vers le fichier CSV
     * @param autoLoad Active/désactive le chargement automatique
     * @param publisherFilter Nom de l'éditeur à filtrer
     */
    public VGSalesLoaderService(
            GameRepository gameRepository,
            @Value("${publisher.vgsales.path}") String vgsalesPath,
            @Value("${publisher.vgsales.auto-load}") boolean autoLoad,
            @Value("${publisher.name}") String publisherFilter) {

        this.gameRepository = gameRepository;
        this.vgsalesPath = vgsalesPath;
        this.autoLoad = autoLoad;
        this.publisherFilter = publisherFilter;
    }

    /**
     * Exécuté automatiquement au démarrage de l'application.
     *
     * @param args Arguments de ligne de commande (non utilisés)
     */
    @Override
    public void run(String... args) {
        if (!autoLoad) {
            logger.info("Chargement automatique VGSales désactivé");
            return;
        }

        logger.info("Démarrage du chargement VGSales depuis: {}", vgsalesPath);

        try {
            List<Game> games = loadGamesFromCSV();
            long count = saveGames(games);

            logger.info("✅ Chargement VGSales terminé : {} jeux importés", count);
        } catch (Exception e) {
            logger.error("❌ Erreur lors du chargement VGSales: {}", e.getMessage(), e);
        }
    }

    /**
     * Charge les jeux depuis le fichier CSV.
     *
     * PARSING :
     * 1. Ouvre le fichier CSV
     * 2. Lit ligne par ligne
     * 3. Filtre selon l'éditeur configuré
     * 4. Crée les entités Game
     *
     * @return Liste des jeux parsés
     * @throws IOException si le fichier est inaccessible
     * @throws CsvException si le format est invalide
     */
    private List<Game> loadGamesFromCSV() throws IOException, CsvException {
        File csvFile = new File(vgsalesPath);

        if (!csvFile.exists()) {
            logger.warn("Fichier VGSales introuvable: {}", vgsalesPath);
            logger.info("Pour importer des jeux, placez vgsales.csv dans: {}", vgsalesPath);
            return new ArrayList<>();
        }

        List<Game> games = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(csvFile))) {
            List<String[]> allLines = reader.readAll();

            // Skip header (première ligne)
            boolean isFirstLine = true;

            for (String[] line : allLines) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Skip header
                }

                // Vérifie que la ligne a assez de colonnes
                if (line.length < 5) {
                    logger.warn("Ligne CSV invalide (colonnes insuffisantes): {}", String.join(",", line));
                    continue;
                }

                try {
                    Game game = parseGameFromCSVLine(line);

                    // Filtre selon l'éditeur (si configuré)
                    if (publisherFilter != null && !publisherFilter.isEmpty()
                        && !publisherFilter.equalsIgnoreCase("*")) {

                        if (game.getPublisher() != null
                            && game.getPublisher().equalsIgnoreCase(publisherFilter)) {
                            games.add(game);
                        }
                    } else {
                        // Aucun filtre, importe tout
                        games.add(game);
                    }

                } catch (Exception e) {
                    logger.warn("Erreur parsing ligne CSV: {} - {}", String.join(",", line), e.getMessage());
                }
            }
        }

        logger.info("Parsé {} jeux depuis le CSV (filtre: {})", games.size(), publisherFilter);
        return games;
    }

    /**
     * Parse une ligne CSV et crée un objet Game.
     *
     * FORMAT CSV :
     * [0] Name
     * [1] Platform
     * [2] Year
     * [3] Genre
     * [4] Publisher
     * [5-9] Sales (ignoré pour l'instant)
     *
     * @param line Tableau de valeurs CSV
     * @return Game créé
     */
    private Game parseGameFromCSVLine(String[] line) {
        String name = line[0].trim();
        String platform = line.length > 1 ? line[1].trim() : "Unknown";
        String genre = line.length > 3 ? line[3].trim() : "Unknown";
        String publisher = line.length > 4 ? line[4].trim() : "Unknown";

        return Game.builder()
            .title(name)
            .platform(platform)
            .genre(genre)
            .publisher(publisher)
            .currentVersion("1.0.0") // Version initiale
            .description("Game from VGSales dataset")
            .build();
    }

    /**
     * Sauvegarde les jeux en base de données.
     *
     * LOGIQUE :
     * - Évite les doublons (vérifie si le titre existe déjà)
     * - Batch insert pour performance
     *
     * @param games Liste des jeux à sauvegarder
     * @return Nombre de jeux sauvegardés
     */
    private long saveGames(List<Game> games) {
        if (games.isEmpty()) {
            return 0;
        }

        long savedCount = 0;

        for (Game game : games) {
            // Vérifie si le jeu existe déjà
            if (!gameRepository.existsByTitle(game.getTitle())) {
                gameRepository.save(game);
                savedCount++;
            } else {
                logger.debug("Jeu déjà existant, ignoré: {}", game.getTitle());
            }
        }

        return savedCount;
    }

    /**
     * Méthode publique pour recharger manuellement les données.
     * Utile pour l'API REST.
     *
     * @return Nombre de jeux importés
     */
    public long reloadData() {
        try {
            List<Game> games = loadGamesFromCSV();
            return saveGames(games);
        } catch (Exception e) {
            logger.error("Erreur lors du rechargement VGSales: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors du chargement VGSales", e);
        }
    }
}

