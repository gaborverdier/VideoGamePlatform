package com.service;

import com.gaming.api.models.PatchModel;
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


    public List<PatchModel> getAllPatches() {
        return patchRepository.findAll().stream()
            .map(patchMapper::toDTO)
            .collect(Collectors.toList());
    }

    public Optional<PatchModel> getPatchById(String id) {
        return patchRepository.findById(id)
            .map(patchMapper::toDTO);
    }

    public List<PatchModel> getPatchesByGame(String gameId) {
        Game game = gameRepository.findById(gameId).orElse(null);
        if (game == null) {
            return List.of();
        }
        return game.getPatches().stream()
            .map(patchMapper::toDTO)
            .collect(Collectors.toList());
    }

    public PatchModel createPatch(Patch patch) {
        // Validation métier : le jeu doit exister
        Game game = gameRepository.findById(patch.getGame().getId())
            .orElseThrow(() -> new IllegalArgumentException("Jeu introuvable avec l'ID: " + patch.getGame().getId()));
        
        patch.setGame(game);
        Patch saved = patchRepository.save(patch);
        PatchModel patchModel = patchMapper.toDTO(saved);

        String topic = "game-patch-released";
        String key = String.valueOf(patchModel.getGameId());

        eventProducer.send(topic, key, patchModel);

        return patchModel;
    }

}
