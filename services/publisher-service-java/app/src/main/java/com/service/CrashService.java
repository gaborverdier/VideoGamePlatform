package com.service;

import com.model.CrashAggregation;
import com.repository.CrashRepository;
import com.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class CrashService {
    @Autowired
    private CrashRepository crashRepository;
    @Autowired
    private GameRepository gameRepository;

    public List<CrashAggregation> getAllCrashes() {
        return crashRepository.findAll();
    }

    public Optional<CrashAggregation> getCrashById(String id) {
        return crashRepository.findById(id);
    }

    public List<CrashAggregation> getCrashesByGame(String gameId) {
        return crashRepository.findByGameIdOrderByWindowStartDesc(gameId);
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
}
