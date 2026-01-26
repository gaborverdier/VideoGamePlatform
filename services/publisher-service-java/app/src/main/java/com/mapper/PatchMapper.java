package com.mapper;

import com.model.Patch;
import com.gaming.api.dto.PatchDTO;

import org.springframework.stereotype.Component;

@Component
public class PatchMapper {
    public PatchDTO toDTO(Patch patch) {
        if (patch == null) throw new IllegalArgumentException("Patch ne peut pas être null");
        if (patch.getGame() == null || patch.getGame().getId() == null)
            throw new IllegalArgumentException("Le jeu associé au patch est obligatoire");
        if (patch.getVersion() == null || patch.getVersion().isEmpty())
            throw new IllegalArgumentException("La version du patch est obligatoire");
        PatchDTO dto = new PatchDTO();
        dto.setId(patch.getId());
        dto.setGameId(patch.getGame().getId());
        dto.setVersion(patch.getVersion());
        dto.setReleaseTime(patch.getReleaseTime());
        dto.setDescription(patch.getDescription());
        return dto;
    }

    public Patch fromDTO(PatchDTO dto, com.model.Game game) {
        if (dto == null) throw new IllegalArgumentException("Le DTO ne peut pas être null");
        if (game == null) throw new IllegalArgumentException("Le jeu associé est obligatoire");
        if (dto.getVersion() == null || dto.getVersion().isEmpty())
            throw new IllegalArgumentException("La version du patch est obligatoire");
        Patch patch = new Patch();
        patch.setId(dto.getId());
        patch.setVersion(dto.getVersion());
        patch.setReleaseTime(dto.getReleaseTime());
        patch.setDescription(dto.getDescription());
        patch.setGame(game);
        return patch;
    }
}
