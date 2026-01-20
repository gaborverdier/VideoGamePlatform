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
        GameCrashReportedEvent event = new GameCrashReportedEvent();
        event.setCrashId(crash.getId() != null ? crash.getId().toString() : "");
        event.setGameId(crash.getGame() != null && crash.getGame().getId() != null ? crash.getGame().getId() : 0L);
        event.setGameTitle(crash.getGame() != null ? crash.getGame().getTitle() : "");
        event.setErrorCode(""); // À adapter si tu as un champ code d'erreur
        event.setErrorMessage(crash.getDescription());
        event.setStackTrace(null); // À adapter si tu as un champ stacktrace
        event.setPlatform(crash.getGame() != null ? crash.getGame().getPlatform() : "");
        event.setGameVersion(crash.getGameVersion()); // À adapter si tu as un champ version dans Game
        // Conversion de la date en timestamp (si crashDate est un String timestamp, sinon adapter)
        try {
            event.setCrashTimestamp(Long.parseLong(crash.getCrashDate()));
        } catch (Exception e) {
            event.setCrashTimestamp(System.currentTimeMillis());
        }
        event.setUserId(null); // À adapter si tu as un champ user
        return event;
    }

    public Crash fromAvro(GameCrashReportedEvent event) {
        Crash crash = new Crash();
        if (event.getCrashId() != null && !event.getCrashId().isEmpty()) {
            try {
                crash.setId(Long.parseLong(event.getCrashId()));
            } catch (NumberFormatException ignored) {}
        }
        crash.setDescription(event.getErrorMessage());
        crash.setCrashDate(String.valueOf(event.getCrashTimestamp()));
        // Récupère le jeu depuis son id
        if (event.getGameId() > 0) {
            crash.setGame(gameRepository.findById(event.getGameId()).orElse(null));
        } else {
            crash.setGame(null);
        }
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
