package com.gaming.platform.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.gaming.platform.dto.GameDTO;
import com.gaming.platform.model.Game;
import com.gaming.platform.repository.GameRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameService {
    private final GameRepository gameRepository;

    public List<GameDTO> getAllAvailableGames() {
        return gameRepository.findByAvailableTrue()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<GameDTO> getGameById(String gameId) {
        return gameRepository.findById(gameId).map(this::convertToDTO);
    }

    public List<GameDTO> searchGames(String title) {
        return gameRepository.findByTitleContainingIgnoreCase(title)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<GameDTO> getGamesByGenre(String genre) {
        return gameRepository.findByTitleContainingIgnoreCase(genre)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<GameDTO> getGamesByPlatform(String platform) {
        return gameRepository.findByPlatform(platform)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private GameDTO convertToDTO(Game game) {
        GameDTO dto = new GameDTO();
        dto.setGameId(game.getGameId());
        dto.setTitle(game.getTitle());
        dto.setPublisher(game.getPublisher());
        dto.setPlatform(game.getPlatform());
        dto.setGenre(game.getGenre());
        dto.setReleaseYear(game.getReleaseYear());
        dto.setPrice(game.getPrice());
        dto.setVersion(game.getVersion());
        dto.setAvailable(game.getAvailable());
        dto.setDescription(game.getDescription());
        return dto;
    }

}
