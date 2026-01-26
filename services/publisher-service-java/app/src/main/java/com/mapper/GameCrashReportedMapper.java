package com.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.repository.GameRepository;
import com.model.Game;

import com.gaming.events.GameCrashReported;
import com.model.Crash;

@Component
public class GameCrashReportedMapper {
    @Autowired
    private GameRepository gameRepository;


    public Crash fromAvro(GameCrashReported avroEvent) {
        if (avroEvent == null) {
            throw new IllegalArgumentException("L'événement Avro ne peut pas être null");
        }

        Crash event = new Crash();
        event.setCrashTime(avroEvent.getCrashTimestamp());
        event.setGameVersion(avroEvent.getGameVersion());
        event.setErrorMessage(avroEvent.getErrorMessage());
        event.setGameVersion(avroEvent.getGameVersion());

        Game game = gameRepository.findById(avroEvent.getGameId()).orElseThrow(() -> new IllegalArgumentException("Jeu introuvable pour l'id fourni: " + avroEvent.getGameId()));
        event.setGame(game);

        return event;
    }

    public GameCrashReported toAvro(Crash event) {
        if (event == null) {
            throw new IllegalArgumentException("L'événement Crash ne peut pas être null");
        }

        GameCrashReported avroEvent = GameCrashReported.newBuilder()
                .setGameId(event.getGame().getId())
                .setCrashTimestamp(event.getCrashTime())
                .setGameVersion(event.getGameVersion())
                .setErrorMessage(event.getErrorMessage())
                .build();

        return avroEvent;
    }
}
