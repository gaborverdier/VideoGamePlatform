package com.gaming.platform.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.gaming.api.models.GameModel;
import com.gaming.platform.model.Library;
import com.gaming.platform.repository.LibraryRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LibraryService {

    private final LibraryRepository libraryRepository;
    private final GameService gameService;

    public LibraryService(LibraryRepository libraryRepository, GameService gameService) {
        this.libraryRepository = libraryRepository;
        this.gameService = gameService;
    }

    public List<Library> listByUser(String userId) {
        return libraryRepository.findByUserId(userId);
    }

    public List<GameModel> getGamesForUser(String userId) {
        return libraryRepository.findByUserId(userId).stream()
                .map(lib -> gameService.getGameById(lib.getGameId()))
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .collect(java.util.stream.Collectors.toList());
    }

    public Library addToLibrary(String userId, String gameId) {
        Library lib = new Library();
        lib.setUserId(userId);
        lib.setGameId(gameId);
        lib.setAddedAt(LocalDateTime.now());
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