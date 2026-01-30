package com.service;

import com.gaming.api.models.CrashAggregationModel;
import com.mapper.CrashAggregationMapper;
import com.model.CrashAggregation;
import java.util.ArrayList;
import com.model.Game;
import com.repository.CrashRepository;
import com.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CrashService {
    @Autowired
    private CrashRepository crashRepository;
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private CrashAggregationMapper crashMapper;

    public List<CrashAggregationModel> getAllCrashes() {
        return crashRepository.findAll().stream()
                .map(crashMapper::toAvro)
                .collect(Collectors.toList());
    }

    public Optional<CrashAggregationModel> getCrashById(String id) {
        return crashRepository.findById(id)
                .map(crashMapper::toAvro);
    }

    public List<CrashAggregationModel> getCrashesByGame(String gameId) {
        return crashRepository.findByGameIdOrderByWindowStartDesc(gameId).stream()
                .map(crashMapper::toAvro)
                .collect(Collectors.toList());
    }

    public CrashAggregation saveCrashAggregation(CrashAggregation crash) {
        // Validation métier : le jeu doit exister
        if (crash.getGameId() == null || crash.getGameId().isEmpty()) {
            throw new IllegalArgumentException("Le crash doit être associé à un jeu valide");
        }
        if (!gameRepository.existsById(crash.getGameId())) {
            log.warn("⚠️ Game {} not found, saving crash aggregation anyway", crash.getGameId());
        }
        return crashRepository.save(crash);
    }

    public List<CrashAggregationModel> getCrashesByPublisher(String publisherId) {
        List<Game> games = gameRepository.findByPublisherId(publisherId);
        List<CrashAggregation> crashes = new ArrayList<CrashAggregation>();

        for (Game game : games) {
            crashes.addAll(crashRepository.findByGameIdOrderByWindowStartDesc(game.getId()));
        }
        return crashes.stream()
                .map(crashMapper::toAvro)
                .collect(Collectors.toList());
    }
}
