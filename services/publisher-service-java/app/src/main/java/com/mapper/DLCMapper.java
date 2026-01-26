package com.mapper;


import com.model.DLC;
import com.gaming.api.models.DLCModel;

import org.springframework.stereotype.Component;

@Component

public class DLCMapper {
    public static DLCModel toDTO(DLC dlc) {
        if (dlc == null) throw new IllegalArgumentException("DLC ne peut pas être null");
        DLCModel dto = new DLCModel();
        dto.setId(dlc.getId());
        dto.setGameId(dlc.getGame() != null ? dlc.getGame().getId() : null);
        dto.setTitle(dlc.getName());
        dto.setReleaseTimeStamp(dlc.getReleaseTime());
        dto.setDescription(dlc.getDescription());
        return dto;
    }

    public static DLC fromDTO(DLCModel dto, com.model.Game game) {
        if (dto == null) throw new IllegalArgumentException("Le DTO ne peut pas être null");
        if (game == null) throw new IllegalArgumentException("Le jeu associé est obligatoire");
        DLC dlc = new DLC();
        dlc.setId(dto.getId());
        dlc.setName(dto.getTitle());
        dlc.setReleaseTime(dto.getReleaseTimeStamp());
        dlc.setDescription(dto.getDescription());
        dlc.setGame(game);
        return dlc;
    }
}
