package com.mapper;

import com.model.Game;
import com.gaming.api.dto.GameDTO;

import org.springframework.stereotype.Component;

@Component
public class GameMapper {
    public GameDTO toDTO(Game game) {
        if (game == null) throw new IllegalArgumentException("Le jeu ne peut pas être null");
        if (game.getId() == null)
            throw new IllegalArgumentException("L'identifiant du jeu est obligatoire");
        if (game.getTitle() == null || game.getTitle().isEmpty())
            throw new IllegalArgumentException("Le titre du jeu est obligatoire");
        GameDTO dto = new GameDTO();
        dto.setGameId(game.getId());
        dto.setTitle(game.getTitle());
        dto.setPublisherName(game.getPublisher() != null ? game.getPublisher().getName() : "");
        dto.setPublisherId(game.getPublisher() != null ? game.getPublisher().getId() : null);
        dto.setPlatform(game.getPlatform());
        dto.setGenre(game.getGenre());
        dto.setReleaseTime(0L); // à compléter si champ disponible
        dto.setPrice(0.0); // à compléter si champ disponible
        dto.setVersion(""); // à compléter si champ disponible
        dto.setAvailable(true); // à adapter selon la logique métier
        dto.setDescription(null); // à compléter si champ disponible
        return dto;
    }

    public Game fromDTO(GameDTO dto, com.model.Publisher publisher) {
        if (dto == null) throw new IllegalArgumentException("Le DTO ne peut pas être null");
        if (dto.getTitle() == null || dto.getTitle().isEmpty())
            throw new IllegalArgumentException("Le titre du jeu est obligatoire");
        if (publisher == null)
            throw new IllegalArgumentException("Le publisher est obligatoire");
        Game game = new Game();
        game.setId(dto.getGameId());
        game.setTitle(dto.getTitle());
        game.setGenre(dto.getGenre());
        game.setPlatform(dto.getPlatform());
        game.setPublisher(publisher);
        // autres champs à compléter si besoin
        return game;
    }
}
