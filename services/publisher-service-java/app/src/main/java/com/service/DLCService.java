package com.service;

import com.gaming.api.models.DLCModel;
import com.mapper.DLCMapper;
import com.model.DLC;
import com.model.Game;
import com.repository.DLCRepository;
import com.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DLCService {
    @Autowired
    private DLCRepository dlcRepository;
    @Autowired
    private GameRepository gameRepository;

    public List<DLCModel> getAllDLCs() {
        return dlcRepository.findAll().stream()
            .map(DLCMapper::toDTO)
            .collect(Collectors.toList());
    }

    public Optional<DLCModel> getDLCById(String id) {
        return dlcRepository.findById(id)
            .map(DLCMapper::toDTO);
    }

    public List<DLCModel> getDLCsByGame(String gameId) {
        Game game = gameRepository.findById(gameId).orElse(null);
        if (game == null) {
            return List.of();
        }
        return game.getDlcs().stream()
            .map(DLCMapper::toDTO)
            .collect(Collectors.toList());
    }

    public DLCModel createDLC(String gameId, DLC dlc) {
        // Validation métier : le jeu doit exister
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Jeu introuvable avec l'ID: " + gameId));
        
        dlc.setGame(game);
        DLC saved = dlcRepository.save(dlc);
        return DLCMapper.toDTO(saved);
    }

    public DLCModel updateDLC(String id, DLC dlcDetails) {
        // Validation métier : le DLC doit exister
        DLC dlc = dlcRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("DLC introuvable avec l'ID: " + id));
        
        dlc.setName(dlcDetails.getName());
        dlc.setReleaseTime(dlcDetails.getReleaseTime());
        dlc.setDescription(dlcDetails.getDescription());
        
        DLC updated = dlcRepository.save(dlc);
        return DLCMapper.toDTO(updated);
    }

    public void deleteDLC(String id) {
        // Validation métier : le DLC doit exister
        if (!dlcRepository.existsById(id)) {
            throw new IllegalArgumentException("DLC introuvable avec l'ID: " + id);
        }
        dlcRepository.deleteById(id);
    }
}
