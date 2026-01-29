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
    @Autowired
    private DLCMapper dlcMapper;

    public List<DLCModel> getAllDLCs() {
        return dlcRepository.findAll().stream()
            .map(dlcMapper::toDTO)
            .collect(Collectors.toList());
    }

    public Optional<DLCModel> getDLCById(String id) {
        return dlcRepository.findById(id)
            .map(dlcMapper::toDTO);
    }

    public List<DLCModel> getDLCsByGame(String gameId) {
        Game game = gameRepository.findById(gameId).orElse(null);
        if (game == null) {
            return List.of();
        }
        return game.getDlcs().stream()
            .map(dlcMapper::toDTO)
            .collect(Collectors.toList());
    }

    public Optional<DLCModel> createDLC(DLC dlc) {
        // Validation métier : le jeu doit exister
        Game game = gameRepository.findById(dlc.getGame().getId())
            .orElseThrow(() -> new IllegalArgumentException("Jeu introuvable avec l'ID: " + dlc.getGame().getId()));
        
        dlc.setGame(game);
        DLC saved = dlcRepository.save(dlc);

        DLCModel dto;
        try {
            dto = dlcMapper.toDTO(saved);

            String key = saved.getId();
            String topic = "dlc-released";
            eventProducer.send(topic, key, dto);
        } catch (Exception e) {
            throw new RuntimeException("Échec de la production de l'événement pour le DLC créé", e);
        }

        return Optional.of(dlcMapper.toDTO(saved));
    }

    public Optional<DLCModel> updateDLC(DLC dlcDetails) {
        // Validation métier : le DLC doit exister
        DLC dlc = dlcRepository.findById(dlcDetails.getId())
            .orElseThrow(() -> new IllegalArgumentException("DLC introuvable avec l'ID: " + dlcDetails.getId()));
        
        dlc.setName(dlcDetails.getName());
        dlc.setReleaseTimeStamp(dlcDetails.getReleaseTimeStamp());
        dlc.setDescription(dlcDetails.getDescription());
        
        DLC updated = dlcRepository.save(dlc);

        DLCModel dto;
        try {
            dto = dlcMapper.toDTO(updated);

            String key = updated.getId();
            String topic = "dlc-updated";
            eventProducer.send(topic, key, dto);
        } catch (Exception e) {
            throw new RuntimeException("Échec de la production de l'événement pour le DLC update", e);
        }
        return Optional.of(dlcMapper.toDTO(updated));
    }

}
