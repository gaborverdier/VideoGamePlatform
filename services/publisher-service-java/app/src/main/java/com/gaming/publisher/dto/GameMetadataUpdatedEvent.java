package com.gaming.publisher.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO représentant un événement de mise à jour de métadonnées.
 *
 * NOTE IMPORTANTE : Classe temporaire, à remplacer par la version
 * générée depuis game-metadata-updated.avsc
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameMetadataUpdatedEvent {
    private String gameId;
    private String gameTitle;
    private String genre;
    private String platform;
    private String description;
    private Long updateTimestamp;
    private String publisher;
}

