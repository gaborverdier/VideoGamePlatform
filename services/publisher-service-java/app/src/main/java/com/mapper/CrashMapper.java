package com.mapper;

import com.model.Crash;
import com.repository.GameRepository;
import com.gaming.api.models.CrashModel;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class CrashMapper {
    @Autowired
    private GameRepository gameRepository;

    public CrashModel toDTO(Crash crash) {
        if (crash == null) throw new IllegalArgumentException("Crash ne peut pas être null");
        if (crash.getGame() == null || crash.getGame().getId() == null)
            throw new IllegalArgumentException("Le jeu associé au crash est obligatoire");

        CrashModel dto = new CrashModel();
        dto.setId(crash.getId());
        dto.setGameId(crash.getGame().getId());
        dto.setCrashTimeStamp(crash.getCrashTimeStamp());
        dto.setGameVersion(crash.getGameVersion());
        return dto;
    }

    public Crash fromDTO(CrashModel dto) {
        if (dto == null) throw new IllegalArgumentException("Le DTO ne peut pas être null");
        if ( dto.getGameId() == null)
            throw new IllegalArgumentException("L'identifiant du jeu est obligatoire et doit être positif");
        Crash crash = new Crash();
        crash.setId(dto.getId());
        crash.setCrashTimeStamp(dto.getCrashTimeStamp());
        crash.setGame(gameRepository.findById(dto.getGameId()).orElseThrow(() -> new IllegalArgumentException("Jeu introuvable pour l'id fourni")));
        crash.setGameVersion(dto.getGameVersion());
        return crash;
    }

    public List<CrashModel> toDTOList(List<Crash> crashes) {
        List<CrashModel> dtos = new ArrayList<>();
        for (Crash crash : crashes) {
            dtos.add(toDTO(crash));
        }
        return dtos;
    }

    public List<Crash> fromDTOList(List<CrashModel> dtos) {
        List<Crash> crashes = new ArrayList<>();
        for (CrashModel dto : dtos) {
            crashes.add(fromDTO(dto));
        }
        return crashes;
    }
}
