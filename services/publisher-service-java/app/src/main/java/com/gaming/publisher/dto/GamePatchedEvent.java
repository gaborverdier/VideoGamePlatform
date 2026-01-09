package com.gaming.publisher.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * DTO représentant un événement de patch de jeu.
 *
 * NOTE IMPORTANTE : Cette classe est temporaire.
 * Dans un environnement de production, elle serait générée automatiquement
 * depuis le schéma Avro game-patched.avsc par le plugin Gradle.
 *
 * Pour utiliser la vraie classe générée :
 * 1. Builder le module common/avro-schemas : ./gradlew build
 * 2. Remplacer cette classe par com.gaming.events.GamePatchedEvent
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GamePatchedEvent {
    private String gameId;
    private String gameTitle;
    private String version;
    private String previousVersion;
    private String changelog;
    private Long patchSize;
    private Long releaseTimestamp;
    private String publisher;
}
