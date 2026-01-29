package org.example.controllers;

import java.time.Instant;
import java.util.UUID;

import org.example.services.KafkaProducerService;

public class PlayerDashboardController {

    private final KafkaProducerService kafkaProducer;

    private String currentSessionId;
    private Instant sessionStartTime;

    // infos du joueur courant (à injecter depuis login)
    private final String userId;
    private final String username;
    private String platform;

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
    public void startGame(String gameId, String gameTitle, String gameVersion, String currentPlatform) {
        currentSessionId = UUID.randomUUID().toString();
        sessionStartTime = Instant.now();
        platform = currentPlatform;

        // TODO? publish session started event
    }

    // ----------------------------
    // STOP
    // ----------------------------
    public void stopGame(String gameId, String gameTitle, String gameVersion) {
        if (currentSessionId == null || sessionStartTime == null) {
            return;
        }

        // TODO? publish session ended event

        currentSessionId = null;
        sessionStartTime = null;
    }

    // ----------------------------
    // CRASH
    // ----------------------------
    public void reportCrash(String gameId,
        String gameVersion,
        int crashCode,
        String message) {

        // include crash code in error message
        String combinedMessage = String.format("code=%d; %s", crashCode, message == null ? "" : message);

        kafkaProducer.publishGameCrashReported(
            UUID.randomUUID().toString(),
            userId,
            gameId,
            gameVersion,
            platform == null ? "unknown" : platform,
            combinedMessage,
            Instant.now().getEpochSecond()
        );

        // reset session
        currentSessionId = null;
        sessionStartTime = null;
    }
}
