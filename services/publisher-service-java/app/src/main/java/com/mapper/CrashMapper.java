package com.mapper;

import com.model.Crash;
import com.repository.GameRepository;
import com.gaming.api.dto.CrashDTO;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class CrashMapper {
    @Autowired
    private GameRepository gameRepository;

    public CrashDTO toDTO(Crash crash) {
        if (crash == null) throw new IllegalArgumentException("Crash ne peut pas être null");
        if (crash.getGame() == null || crash.getGame().getId() == null)
            throw new IllegalArgumentException("Le jeu associé au crash est obligatoire");

        CrashDTO dto = new CrashDTO();
        dto.setId(crash.getId());
        dto.setGameId(crash.getGame().getId());
        dto.setCrashTime(crash.getCrashTime());
        dto.setGameVersion(crash.getGameVersion());
        return dto;
    }

    public Crash fromDTO(CrashDTO dto) {
        if (dto == null) throw new IllegalArgumentException("Le DTO ne peut pas être null");
        if ( dto.getGameId() <= 0)
            throw new IllegalArgumentException("L'identifiant du jeu est obligatoire et doit être positif");
        if (dto.getDescription() == null)
            throw new IllegalArgumentException("La description du crash est obligatoire");
        Crash crash = new Crash();
        crash.setId(dto.getId());
        crash.setCrashTime(dto.getCrashTime());
        crash.setGame(gameRepository.findById(dto.getGameId()).orElseThrow(() -> new IllegalArgumentException("Jeu introuvable pour l'id fourni")));
        crash.setGameVersion(dto.getGameVersion());
        return crash;
    }

    public List<CrashDTO> toDTOList(List<Crash> crashes) {
        List<CrashDTO> dtos = new ArrayList<>();
        for (Crash crash : crashes) {
            dtos.add(toDTO(crash));
        }
        return dtos;
    }

    public List<Crash> fromDTOList(List<CrashDTO> dtos) {
        List<Crash> crashes = new ArrayList<>();
        for (CrashDTO dto : dtos) {
            crashes.add(fromDTO(dto));
        }
        return crashes;
    }
}
