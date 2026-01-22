package com.mapper;

import com.model.Crash;
import com.repository.GameRepository;
import com.gaming.events.GameCrashReportedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class CrashMapper {
    @Autowired
    private GameRepository gameRepository;

    public GameCrashReportedEvent toAvro(Crash crash) {
        if (crash == null) throw new IllegalArgumentException("Crash ne peut pas être null");
        if (crash.getGame() == null || crash.getGame().getId() == null)
            throw new IllegalArgumentException("Le jeu associé au crash est obligatoire");
        if (crash.getDescription() == null || crash.getDescription().isEmpty())
            throw new IllegalArgumentException("La description du crash est obligatoire");
        GameCrashReportedEvent event = new GameCrashReportedEvent();
        event.setCrashId(crash.getId() != null ? crash.getId().toString() : "");
        event.setGameId(crash.getGame().getId());
        event.setGameTitle(crash.getGame().getTitle() != null ? crash.getGame().getTitle() : "");
        event.setErrorCode("");
        event.setErrorMessage(crash.getDescription());
        event.setStackTrace(null);
        event.setPlatform(crash.getGame().getPlatform() != null ? crash.getGame().getPlatform() : "");
        event.setGameVersion(crash.getGameVersion());
        try {
            event.setCrashTimestamp(Long.parseLong(crash.getCrashDate()));
        } catch (Exception e) {
            throw new IllegalArgumentException("La date du crash est invalide");
        }
        event.setUserId(null);
        return event;
    }

    public Crash fromAvro(GameCrashReportedEvent event) {
        if (event == null) throw new IllegalArgumentException("L'événement ne peut pas être null");
        if (event.getGameId() <= 0)
            throw new IllegalArgumentException("L'identifiant du jeu est obligatoire et doit être positif");
        if (event.getErrorMessage() == null || event.getErrorMessage().isEmpty())
            throw new IllegalArgumentException("Le message d'erreur est obligatoire");
        Crash crash = new Crash();
        if (event.getCrashId() != null && !event.getCrashId().isEmpty()) {
            try {
                crash.setId(Long.parseLong(event.getCrashId()));
            } catch (NumberFormatException ignored) {}
        }
        crash.setDescription(event.getErrorMessage());
        crash.setCrashDate(String.valueOf(event.getCrashTimestamp()));
        crash.setGame(gameRepository.findById(event.getGameId()).orElseThrow(() -> new IllegalArgumentException("Jeu introuvable pour l'id fourni")));
        crash.setGameVersion(event.getGameVersion());
        return crash;
    }

    public List<GameCrashReportedEvent> toAvroList(List<Crash> crashes) {
        List<GameCrashReportedEvent> events = new ArrayList<>();
        for (Crash crash : crashes) {
            events.add(toAvro(crash));
        }
        return events;
    }

    public List<Crash> fromAvroList(List<GameCrashReportedEvent> events, Map<String, com.model.Game> gameMap) {
        List<Crash> crashes = new ArrayList<>();
        for (GameCrashReportedEvent event : events) {
            crashes.add(fromAvro(event));
        }
        return crashes;
    }
}
