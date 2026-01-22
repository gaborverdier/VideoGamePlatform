package com.mapper;

import com.model.Game;
import com.gaming.events.GameMetadataUpdatedEvent;

import org.springframework.stereotype.Component;

@Component
public class GameMapper {
    public GameMetadataUpdatedEvent toAvro(Game game) {
        if (game == null) throw new IllegalArgumentException("Le jeu ne peut pas être null");
        if (game.getId() == null)
            throw new IllegalArgumentException("L'identifiant du jeu est obligatoire");
        if (game.getTitle() == null || game.getTitle().isEmpty())
            throw new IllegalArgumentException("Le titre du jeu est obligatoire");
        GameMetadataUpdatedEvent event = new GameMetadataUpdatedEvent();
        event.setGameId(game.getId().toString());
        event.setGameTitle(game.getTitle());
        event.setGenre(game.getGenre());
        event.setPlatform(game.getPlatform());
        event.setDescription(null);
        event.setUpdateTimestamp(System.currentTimeMillis());
        event.setPublisher(game.getPublisher() != null ? game.getPublisher().getName() : "");
        return event;
    }

    public Game fromAvro(GameMetadataUpdatedEvent event, com.model.Publisher publisher) {
        if (event == null) throw new IllegalArgumentException("L'événement ne peut pas être null");
        if (event.getGameTitle() == null || event.getGameTitle().isEmpty())
            throw new IllegalArgumentException("Le titre du jeu est obligatoire");
        if (publisher == null)
            throw new IllegalArgumentException("Le publisher est obligatoire");
        Game game = new Game();
        if (event.getGameId() != null && !event.getGameId().isEmpty()) {
            try {
                game.setId(Long.parseLong(event.getGameId()));
            } catch (NumberFormatException ignored) {}
        }
        game.setTitle(event.getGameTitle());
        game.setGenre(event.getGenre());
        game.setPlatform(event.getPlatform());
        game.setPublisher(publisher);
        return game;
    }
}
