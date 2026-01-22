package com.service;

import com.model.Patch;
import com.model.Game;

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
        Game game = gameRepository.findById(gameId).orElse(null);
        if (game == null) {
            return List.of();
        }
        return game.getPatches();
    }

    public Patch createPatch(Long gameId, Patch patch) {
        // Validation métier : le jeu doit exister
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Jeu introuvable avec l'ID: " + gameId));
        
        patch.setGame(game);
        return patchRepository.save(patch);
    }

    public Patch updatePatch(Long id, Patch patchDetails) {
        // Validation métier : le patch doit exister
        Patch patch = patchRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Patch introuvable avec l'ID: " + id));
        
        patch.setVersion(patchDetails.getVersion());
        patch.setReleaseDate(patchDetails.getReleaseDate());
        patch.setDescription(patchDetails.getDescription());
        
        return patchRepository.save(patch);
    }

    public void deletePatch(Long id) {
        // Validation métier : le patch doit exister
        if (!patchRepository.existsById(id)) {
            throw new IllegalArgumentException("Patch introuvable avec l'ID: " + id);
        }
        patchRepository.deleteById(id);
    }
}
