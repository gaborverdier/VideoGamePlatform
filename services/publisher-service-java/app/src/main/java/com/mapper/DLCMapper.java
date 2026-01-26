package com.mapper;


import com.model.DLC;
import com.gaming.api.dto.DLCDTO;

public class DLCMapper {
    public static DLCDTO toDTO(DLC dlc) {
        if (dlc == null) throw new IllegalArgumentException("DLC ne peut pas être null");
        DLCDTO dto = new DLCDTO();
        dto.setId(dlc.getId());
        dto.setGameId(dlc.getGame() != null ? dlc.getGame().getId() : null);
        dto.setTitle(dlc.getName());
        dto.setReleaseTime(dlc.getReleaseTime());
        dto.setDescription(dlc.getDescription());
        return dto;
    }

    public static DLC fromDTO(DLCDTO dto, com.model.Game game) {
        if (dto == null) throw new IllegalArgumentException("Le DTO ne peut pas être null");
        if (game == null) throw new IllegalArgumentException("Le jeu associé est obligatoire");
        DLC dlc = new DLC();
        dlc.setId(dto.getId());
        dlc.setName(dto.getTitle());
        dlc.setReleaseTime(dto.getReleaseTime());
        dlc.setDescription(dto.getDescription());
        dlc.setGame(game);
        return dlc;
    }
}
