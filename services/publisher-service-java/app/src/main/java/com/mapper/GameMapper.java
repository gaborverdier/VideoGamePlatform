package com.mapper;

import com.model.Game;
import com.gaming.events.GameMetadataUpdatedEvent;

import org.springframework.stereotype.Component;

@Component
public class GameMapper {
    public GameMetadataUpdatedEvent toAvro(Game game) {
        GameMetadataUpdatedEvent event = new GameMetadataUpdatedEvent();
        event.setGameId(game.getId() != null ? game.getId().toString() : "");
        event.setGameTitle(game.getTitle() != null ? game.getTitle() : "");
        event.setGenre(game.getGenre());
        event.setPlatform(game.getPlatform());
        event.setDescription(null); // À adapter si tu ajoutes une description dans Game
        event.setUpdateTimestamp(System.currentTimeMillis());
        event.setPublisher(game.getPublisher() != null ? game.getPublisher().getName() : "");
        return event;
    }

    public Game fromAvro(GameMetadataUpdatedEvent event, com.model.Publisher publisher) {
        Game game = new Game();
        if (event.getGameId() != null && !event.getGameId().isEmpty()) {
            try {
                game.setId(Long.parseLong(event.getGameId()));
            } catch (NumberFormatException ignored) {}
        }
        game.setTitle(event.getGameTitle());
        game.setGenre(event.getGenre());
        game.setPlatform(event.getPlatform());
        game.setPublisher(publisher); // Publisher récupéré en base via event.getPublisher()
        return game;
    }
}
