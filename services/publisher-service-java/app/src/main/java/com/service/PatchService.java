package com.service;

import com.gaming.api.dto.PatchDTO;
import com.mapper.PatchMapper;
import com.model.Patch;
import com.model.Game;
import com.repository.PatchRepository;
import com.repository.GameRepository;
import com.producer.EventProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PatchService {
    @Autowired
    private PatchRepository patchRepository;
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private PatchMapper patchMapper;
    @Autowired
    private EventProducer eventProducer;


    public List<PatchDTO> getAllPatches() {
        return patchRepository.findAll().stream()
            .map(patchMapper::toDTO)
            .collect(Collectors.toList());
    }

    public Optional<PatchDTO> getPatchById(Long id) {
        return patchRepository.findById(id)
            .map(patchMapper::toDTO);
    }

    public List<PatchDTO> getPatchesByGame(Long gameId) {
        Game game = gameRepository.findById(gameId).orElse(null);
        if (game == null) {
            return List.of();
        }
        return game.getPatches().stream()
            .map(patchMapper::toDTO)
            .collect(Collectors.toList());
    }

    public PatchDTO createPatch(Long gameId, Patch patch) {
        // Validation métier : le jeu doit exister
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Jeu introuvable avec l'ID: " + gameId));
        
        patch.setGame(game);
        Patch saved = patchRepository.save(patch);
        PatchDTO patchDTO = patchMapper.toDTO(saved);

        String topic = "game-patch-released";
        String key = String.valueOf(patchDTO.getId());

        eventProducer.send(topic, key, patchDTO);

        return patchDTO;
    }

    public PatchDTO updatePatch(Long id, Patch patchDetails) {
        // Validation métier : le patch doit exister
        Patch patch = patchRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Patch introuvable avec l'ID: " + id));
        
        patch.setVersion(patchDetails.getVersion());
        patch.setReleaseTime(patchDetails.getReleaseTime());
        patch.setDescription(patchDetails.getDescription());
        
        Patch updated = patchRepository.save(patch);
        return patchMapper.toDTO(updated);
    }

    public void deletePatch(Long id) {
        // Validation métier : le patch doit exister
        if (!patchRepository.existsById(id)) {
            throw new IllegalArgumentException("Patch introuvable avec l'ID: " + id);
        }
        patchRepository.deleteById(id);
    }
}
