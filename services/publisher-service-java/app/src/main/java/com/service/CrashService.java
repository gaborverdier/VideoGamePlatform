package com.service;

import com.model.Crash;
import com.repository.CrashRepository;
import com.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CrashService {
    @Autowired
    private CrashRepository crashRepository;
    @Autowired
    private GameRepository gameRepository;

    public List<Crash> getAllCrashes() {
        return crashRepository.findAll();
    }

    public Optional<Crash> getCrashById(Long id) {
        return crashRepository.findById(id);
    }

    public List<Crash> getCrashesByGame(Long gameId) {
        return gameRepository.findById(gameId)
                .map(game -> game.getCrashes())
                .orElse(List.of());
    }

    public Crash createCrash(Long gameId, Crash crash) {
        return gameRepository.findById(gameId).map(game -> {
            crash.setGame(game);
            return crashRepository.save(crash);
        }).orElse(null);
    }

    public Crash updateCrash(Long id, Crash crashDetails) {
        return crashRepository.findById(id).map(crash -> {
            crash.setDescription(crashDetails.getDescription());
            crash.setCrashDate(crashDetails.getCrashDate());
            return crashRepository.save(crash);
        }).orElse(null);
    }

    public void deleteCrash(Long id) {
        crashRepository.deleteById(id);
    }
}
