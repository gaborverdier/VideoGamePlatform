package org.example.controllers;

import org.example.services.KafkaProducerService;

import java.time.Instant;
import java.util.UUID;

public class PlayerDashboardController {

    private final KafkaProducerService kafkaProducer;

    private String currentSessionId;
    private Instant sessionStartTime;

    // infos du joueur courant (à injecter depuis login)
    private final String userId;
    private final String username;

    public PlayerDashboardController(KafkaProducerService kafkaProducer,
                                     String userId,
                                     String username) {
        this.kafkaProducer = kafkaProducer;
        this.userId = userId;
        this.username = username;
    }

    // ----------------------------
    // PLAY
    // ----------------------------
    public void startGame(String gameId, String gameTitle, String gameVersion) {
        currentSessionId = UUID.randomUUID().toString();
        sessionStartTime = Instant.now();

        kafkaProducer.publishSessionStarted(
                userId,
                username,
                gameId,
                gameTitle,
                gameVersion
        );
    }

    // ----------------------------
    // STOP
    // ----------------------------
    public void stopGame(String gameId, String gameTitle, String gameVersion) {
        if (currentSessionId == null || sessionStartTime == null) {
            return;
        }

        long durationSeconds =
                Instant.now().getEpochSecond() - sessionStartTime.getEpochSecond();

        kafkaProducer.publishSessionEnded(
                currentSessionId,
                userId,
                username,
                gameId,
                gameTitle,
                gameVersion,
                durationSeconds
        );

        currentSessionId = null;
        sessionStartTime = null;
    }

    // ----------------------------
    // CRASH
    // ----------------------------
    public void reportCrash(String gameId,
                            String gameTitle,
                            String gameVersion,
                            String message) {

        kafkaProducer.publishCrashReported(
                userId,
                username,
                gameId,
                gameTitle,
                gameVersion,
                message
        );

        // reset session
        currentSessionId = null;
        sessionStartTime = null;
    }
}
