package com.gaming.platform.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.gaming.api.models.DLCModel;
import com.gaming.platform.model.DLC;
import com.gaming.platform.repository.DLCRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DLCService {
    private final DLCRepository dlcRepository;

    public List<DLCModel> getDLCsByGameId(String gameId) {
        return dlcRepository.findByGameId(gameId)
                .stream()
                .map(this::toDLCModel)
                .collect(Collectors.toList());
    }

    public DLCModel createDLC(DLCModel dlcModel) {
        DLC dlc = new DLC();
        // assign an id since DLC.id is a String and not auto-generated
        dlc.setId(UUID.randomUUID().toString());
        dlc.setTitle(dlcModel.getTitle());
        dlc.setGameId(dlcModel.getGameId());
        dlc.setReleaseTimeStamp(dlcModel.getReleaseTimeStamp());
        dlc.setDescription(dlcModel.getDescription());
        // set price from model if available, otherwise default to 0.0

        dlc.setPrice(BigDecimal.valueOf(0.0));

        DLC savedDLC = dlcRepository.save(dlc);
        log.info("Created new DLC with ID {}", savedDLC.getId());
        return toDLCModel(savedDLC);
    }

    private DLCModel toDLCModel(DLC dlc) {
        return DLCModel.newBuilder()
                .setId(dlc.getId())
                .setTitle(dlc.getTitle())
                .setGameId(dlc.getGameId())
                .setReleaseTimeStamp(dlc.getReleaseTimeStamp())
                .setDescription(dlc.getDescription())
                .build();
    }
}