package com.service;

import com.model.DLC;
import com.repository.DLCRepository;
import com.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DLCService {
    @Autowired
    private DLCRepository dlcRepository;
    @Autowired
    private GameRepository gameRepository;

    public List<DLC> getAllDLCs() {
        return dlcRepository.findAll();
    }

    public Optional<DLC> getDLCById(Long id) {
        return dlcRepository.findById(id);
    }

    public List<DLC> getDLCsByGame(Long gameId) {
        return gameRepository.findById(gameId)
                .map(game -> game.getDlcs())
                .orElse(List.of());
    }

    public DLC createDLC(Long gameId, DLC dlc) {
        return gameRepository.findById(gameId).map(game -> {
            dlc.setGame(game);
            return dlcRepository.save(dlc);
        }).orElse(null);
    }

    public DLC updateDLC(Long id, DLC dlcDetails) {
        return dlcRepository.findById(id).map(dlc -> {
            dlc.setName(dlcDetails.getName());
            dlc.setReleaseDate(dlcDetails.getReleaseDate());
            dlc.setDescription(dlcDetails.getDescription());
            return dlcRepository.save(dlc);
        }).orElse(null);
    }

    public void deleteDLC(Long id) {
        dlcRepository.deleteById(id);
    }
}
