package com.gaming.platform.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gaming.api.models.GameModel;
import com.gaming.platform.model.Game;
import com.gaming.platform.model.Library;
import com.gaming.platform.model.User;
import com.gaming.platform.repository.GameRepository;
import com.gaming.platform.repository.LibraryRepository;
import com.gaming.platform.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LibraryService {

    private final LibraryRepository libraryRepository;
    private final GameService gameService;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;

    public LibraryService(LibraryRepository libraryRepository, GameService gameService, GameRepository gameRepository,
            UserRepository userRepository) {
        this.libraryRepository = libraryRepository;
        this.gameService = gameService;
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
    }

    public List<Library> listByUser(String userId) {
        return libraryRepository.findByUserId(userId);
    }

    public List<GameModel> getGamesForUser(String userId) {
        return libraryRepository.findByUserId(userId).stream()
                .map(lib -> gameService.getGameById(lib.getGameId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Transactional
    public Library addToLibrary(String userId, String gameId) {
        // Reject if the user already has this game in their library
        Library existing = libraryRepository.findByUserIdAndGameId(userId, gameId);
        if (existing != null) {
            throw new IllegalArgumentException("Game already in library: " + gameId);
        }

        Library lib = new Library();
        lib.setUserId(userId);
        lib.setGameId(gameId);
        lib.setAddedAt(LocalDateTime.now());
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found: " + gameId));

        // Load user and check balance
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        double price = game.getPrice() != null ? game.getPrice().doubleValue() : 0.0;
        Double balance = user.getBalance();
        if (balance == null || balance < price) {
            throw new IllegalStateException("Insufficient balance: required=" + price + " available=" + balance);
        }
        user.setBalance(balance - price);
        userRepository.save(user);

        // remove from wishlist if present
        libraryRepository.deleteFromWishlistIfExists(userId, gameId);

        return libraryRepository.save(lib);
    }

    public Library markInstalledByUserAndGame(String userId, String gameId) {
        Library lib = libraryRepository.findByUserIdAndGameId(userId, gameId);
        if (lib == null) {
            lib = new Library();
            lib.setUserId(userId);
            lib.setGameId(gameId);
            lib.setAddedAt(LocalDateTime.now());
        }

        lib.setInstalled(true);
        lib.setInstalledAt(LocalDateTime.now());
        Library saved = libraryRepository.save(lib);
        log.info("Library item for user {} game {} marked as installed", userId, gameId);
        return saved;
    }
}