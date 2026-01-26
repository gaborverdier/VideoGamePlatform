package com.service;

import com.model.Crash;
import com.model.Game;
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

    public Optional<Crash> getCrashById(String id) {
        return crashRepository.findById(id);
    }

    public List<Crash> getCrashesByGame(String gameId) {
        Game game = gameRepository.findById(gameId).orElse(null);
        if (game == null) {
            return List.of();
        }
        return game.getCrashes();
    }

    public Crash createCrash(Crash crash) {
        // Validation métier : le jeu doit exister
        if (crash.getGame() == null || crash.getGame().getId() == null) {
            throw new IllegalArgumentException("Le crash doit être associé à un jeu valide");
        }
        if (!gameRepository.existsById(crash.getGame().getId())) {
            throw new IllegalArgumentException("Le jeu avec l'ID " + crash.getGame().getId() + " n'existe pas");
        }
        return crashRepository.save(crash);
    }

    public Crash updateCrash(String id, Crash crashDetails) {
        Crash crash = crashRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Crash introuvable avec l'ID: " + id));
        
        crash.setCrashTime(crashDetails.getCrashTime());
        crash.setGameVersion(crashDetails.getGameVersion());
        
        return crashRepository.save(crash);
    }

    public void deleteCrash(String id) {
        // Validation métier : le crash doit exister
        if (!crashRepository.existsById(id)) {
            throw new IllegalArgumentException("Crash introuvable avec l'ID: " + id);
        }
        crashRepository.deleteById(id);
    }
}
