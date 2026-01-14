package com.vgp.client.service;

import com.vgp.client.dto.response.GameDto;
import com.vgp.shared.entity.Game;
import com.vgp.shared.repository.GameRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GameStoreService {
    
    private static final Logger logger = LoggerFactory.getLogger(GameStoreService.class);
    
    private final GameRepository gameRepository;
    
    public GameStoreService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }
    
    public List<GameDto> findAllGames() {
        logger.debug("Fetching all games");
        List<Game> games = gameRepository.findAll();
        return games.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
    }
    
    public Optional<GameDto> findById(Integer id) {
        logger.debug("Fetching game with id: {}", id);
        return gameRepository.findById(id)
                            .map(this::convertToDto);
    }
    
    public List<GameDto> searchByTitle(String query) {
        logger.debug("Searching games with title containing: {}", query);
        List<Game> games = gameRepository.searchByTitle(query);
        return games.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
    }
    
    public List<GameDto> findByEditorId(Integer editorId) {
        logger.debug("Fetching games by editor id: {}", editorId);
        List<Game> games = gameRepository.findByEditorId(editorId);
        return games.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
    }
    
    private GameDto convertToDto(Game game) {
        return new GameDto(
            game.getId(),
            game.getName(),
            game.getEditor() != null ? game.getEditor().getId() : null,
            game.getEditor() != null ? game.getEditor().getName() : null
        );
    }
}
