package com.mapper;


import com.model.DLC;
import com.gaming.api.models.DLCModel;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import com.repository.GameRepository;
import com.model.Game;


@Component
public class DLCMapper {
    @Autowired
    private GameRepository gameRepository;

    public DLCModel toDTO(DLC dlc) {
        if (dlc == null) throw new IllegalArgumentException("DLC ne peut pas être null");
        DLCModel dto = new DLCModel();
        dto.setId(dlc.getId());
        dto.setGameId(dlc.getGame() != null ? dlc.getGame().getId() : null);
        dto.setTitle(dlc.getName());
        dto.setReleaseTimeStamp(dlc.getReleaseTimeStamp());
        dto.setDescription(dlc.getDescription());
        dto.setPrice(dlc.getPrice());
        return dto;
    }

    public DLC fromDTO(DLCModel dto) {
        if (dto == null) throw new IllegalArgumentException("Le DTO ne peut pas être null");

        DLC dlc = new DLC();
        dlc.setId(dto.getId());
        dlc.setName(dto.getTitle());
        dlc.setReleaseTimeStamp(dto.getReleaseTimeStamp());
        dlc.setDescription(dto.getDescription());
        Game game = gameRepository.findById(dto.getGameId())
            .orElseThrow(() -> new IllegalArgumentException("Jeu introuvable avec l'ID: " + dto.getGameId()));
        dlc.setGame(game);
        dlc.setPrice(dto.getPrice());
        return dlc;
    }
}
