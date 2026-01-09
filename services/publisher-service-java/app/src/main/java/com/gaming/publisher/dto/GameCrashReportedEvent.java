package com.gaming.publisher.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO représentant un événement de crash de jeu.
 *
 * NOTE IMPORTANTE : Classe temporaire, à remplacer par la version
 * générée depuis game-crash-reported.avsc
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameCrashReportedEvent {
    private String crashId;
    private String gameId;
    private String gameTitle;
    private String errorCode;
    private String errorMessage;
    private String stackTrace;
    private String platform;
    private String gameVersion;
    private Long crashTimestamp;
    private String userId;
}

