package com.service;

import com.gaming.api.models.DLCModel;
import com.mapper.DLCMapper;
import com.model.DLC;
import com.model.Game;
import com.producer.EventProducer;
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
    @Autowired
    private EventProducer eventProducer;

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

        DLCModel dto;
        try {
            dto = DLCMapper.toDTO(saved);

            String key = saved.getId();
            String topic = "dlc-released";
            eventProducer.send(topic, key, dto);
        } catch (Exception e) {
            throw new RuntimeException("Échec de la production de l'événement pour le DLC créé", e);
        }

        return DLCMapper.toDTO(saved);
    }

    public DLCModel updateDLC(String id, DLC dlcDetails) {
        // Validation métier : le DLC doit exister
        DLC dlc = dlcRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("DLC introuvable avec l'ID: " + id));
        
        dlc.setName(dlcDetails.getName());
        dlc.setReleaseTimeStamp(dlcDetails.getReleaseTimeStamp());
        dlc.setDescription(dlcDetails.getDescription());
        
        DLC updated = dlcRepository.save(dlc);

        DLCModel dto;
        try {
            dto = DLCMapper.toDTO(updated);

            String key = updated.getId();
            String topic = "dlc-updated";
            eventProducer.send(topic, key, dto);
        } catch (Exception e) {
            throw new RuntimeException("Échec de la production de l'événement pour le DLC update", e);
        }
        return DLCMapper.toDTO(updated);
    }

}
