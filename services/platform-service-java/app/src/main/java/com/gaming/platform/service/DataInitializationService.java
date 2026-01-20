package com.gaming.platform.service;

import com.gaming.platform.model.Game;
import com.gaming.platform.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataInitializationService {

    private final GameRepository gameRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeData() {
        if (gameRepository.count() == 0) {
            log.info("Initializing sample game data...");
            seedGames();
            log.info("Sample game data initialized successfully");
        } else {
            log.info("Database already contains data, skipping initialization");
        }
    }

    private void seedGames() {
        List<Game> games = Arrays.asList(
                createGame("The Legend of Zelda: Breath of the Wild", "Nintendo", "Switch", "Action", 2017, 59.99),
                createGame("Grand Theft Auto V", "Rockstar Games", "PC", "Action", 2013, 29.99),
                createGame("Minecraft", "Mojang", "PC", "Sandbox", 2011, 26.95),
                createGame("The Witcher 3: Wild Hunt", "CD Projekt", "PC", "RPG", 2015, 39.99),
                createGame("Red Dead Redemption 2", "Rockstar Games", "PS4", "Action", 2018, 59.99),
                createGame("Cyberpunk 2077", "CD Projekt", "PC", "RPG", 2020, 59.99),
                createGame("Elden Ring", "FromSoftware", "PC", "RPG", 2022, 59.99),
                createGame("God of War", "Sony", "PS4", "Action", 2018, 49.99),
                createGame("Super Mario Odyssey", "Nintendo", "Switch", "Platform", 2017, 59.99),
                createGame("Halo Infinite", "Microsoft", "Xbox", "Shooter", 2021, 59.99));

        gameRepository.saveAll(games);
        log.info("Seeded {} games into database", games.size());
    }

    private Game createGame(String title, String publisher, String platform,
            String genre, int releaseYear, double price) {
        Game game = new Game();
        game.setTitle(title);
        game.setPublisher(publisher);
        game.setPlatform(platform);
        game.setGenre(genre);
        game.setReleaseYear(releaseYear);
        game.setPrice(BigDecimal.valueOf(price));
        game.setVersion("1.0.0");
        game.setAvailable(true);
        game.setLastUpdated(LocalDateTime.now());
        game.setDescription("An exciting " + genre.toLowerCase() + " game from " + publisher);
        return game;
    }
}