package com.service;

import com.model.Patch;
import com.repository.PatchRepository;
import com.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PatchService {
    @Autowired
    private PatchRepository patchRepository;
    @Autowired
    private GameRepository gameRepository;

    public List<Patch> getAllPatches() {
        return patchRepository.findAll();
    }

    public Optional<Patch> getPatchById(Long id) {
        return patchRepository.findById(id);
    }

    public List<Patch> getPatchesByGame(Long gameId) {
        return gameRepository.findById(gameId)
                .map(game -> game.getPatches())
                .orElse(List.of());
    }

    public Patch createPatch(Long gameId, Patch patch) {
        return gameRepository.findById(gameId).map(game -> {
            patch.setGame(game);
            return patchRepository.save(patch);
        }).orElse(null);
    }

    public Patch updatePatch(Long id, Patch patchDetails) {
        return patchRepository.findById(id).map(patch -> {
            patch.setVersion(patchDetails.getVersion());
            patch.setReleaseDate(patchDetails.getReleaseDate());
            patch.setDescription(patchDetails.getDescription());
            return patchRepository.save(patch);
        }).orElse(null);
    }

    public void deletePatch(Long id) {
        patchRepository.deleteById(id);
    }
}
