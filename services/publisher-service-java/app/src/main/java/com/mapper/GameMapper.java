package com.mapper;

import com.model.Game;
import com.gaming.api.models.GameModel;

import org.springframework.stereotype.Component;

@Component
public class GameMapper {
    public GameModel toDTO(Game game) {
        if (game == null) throw new IllegalArgumentException("Le jeu ne peut pas être null");
        if (game.getId() == null)
            throw new IllegalArgumentException("L'identifiant du jeu est obligatoire");
        if (game.getTitle() == null || game.getTitle().isEmpty())
            throw new IllegalArgumentException("Le titre du jeu est obligatoire");
        GameModel dto = new GameModel();
        dto.setGameId(game.getId());
        dto.setTitle(game.getTitle());
        dto.setPublisherName(game.getPublisher() != null ? game.getPublisher().getName() : "");
        dto.setPublisherId(game.getPublisher() != null ? game.getPublisher().getId() : null);
        dto.setPlatform(game.getPlatform());
        dto.setGenre(game.getGenre());
        dto.setReleaseTimeStamp(0L); // à compléter si champ disponible
        dto.setPrice(0.0); // à compléter si champ disponible
        dto.setVersion(""); // à compléter si champ disponible
        dto.setDescription(null); // à compléter si champ disponible
        return dto;
    }

    public Game fromDTO(GameModel dto, com.model.Publisher publisher) {
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
