package com.mapper;

import com.model.Game;
import com.gaming.api.models.GameModel;
import com.gaming.api.requests.GameReleased;
import com.model.Publisher;

import org.springframework.beans.factory.annotation.Autowired;
import com.repository.PublisherRepository;

import org.springframework.stereotype.Component;

@Component
public class GameMapper {
    @Autowired
    private PublisherRepository publisherRepository;

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
        dto.setReleaseTimeStamp(game.getReleaseTimeStamp() != null ? game.getReleaseTimeStamp() : 0L);
        dto.setPrice(game.getPrice() != null ? game.getPrice() : 0.0);
        dto.setVersion(game.getVersion() != null ? game.getVersion() : "");
        dto.setDescription(null);
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
        game.setReleaseTimeStamp(dto.getReleaseTimeStamp());
        game.setPrice(dto.getPrice());
        game.setVersion(dto.getVersion());
        game.setPublisher(publisher);
        return game;
    }

    public Game fromReleaseModel(GameReleased releaseModel) {
        if (releaseModel == null) throw new IllegalArgumentException("Le modèle de sortie ne peut pas être null");
        if (releaseModel.getTitle() == null || releaseModel.getTitle().isEmpty())
            throw new IllegalArgumentException("Le titre du jeu est obligatoire");
        Publisher publisher = publisherRepository.findById(releaseModel.getPublisherId())
            .orElseThrow(() -> new IllegalArgumentException("Le publisher avec l'ID spécifié est introuvable"));
        Game game = new Game();
        game.setTitle(releaseModel.getTitle());
        game.setGenre(releaseModel.getGenre());
        game.setPlatform(releaseModel.getPlatform());
        game.setReleaseTimeStamp(releaseModel.getReleaseTimeStamp());
        game.setPrice(releaseModel.getPrice());
        game.setVersion(releaseModel.getVersion());
        game.setPublisher(publisher);
        if (releaseModel.getDescription() != null) {
            game.setDescription(releaseModel.getDescription());
        }
        return game;
    }
}
