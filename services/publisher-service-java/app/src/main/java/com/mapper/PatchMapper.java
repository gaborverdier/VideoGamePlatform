package com.mapper;

import com.model.Patch;
import com.gaming.events.GamePatchedEvent;

import org.springframework.stereotype.Component;

@Component
public class PatchMapper {
    public GamePatchedEvent toAvro(Patch patch) {
        GamePatchedEvent event = new GamePatchedEvent();
        event.setGameId(patch.getGame() != null && patch.getGame().getId() != null ? patch.getGame().getId().toString() : "");
        event.setGameTitle(patch.getGame() != null ? patch.getGame().getTitle() : "");
        event.setVersion(patch.getVersion() != null ? patch.getVersion() : "");
        event.setPreviousVersion(null); // À adapter si tu as l'info
        event.setChangelog(patch.getDescription() != null ? patch.getDescription() : "");
        event.setPatchSize(0L); // À adapter si tu as l'info
        event.setReleaseTimestamp(System.currentTimeMillis());
        event.setPublisher(patch.getGame() != null && patch.getGame().getPublisher() != null ? patch.getGame().getPublisher().getName() : "");
        return event;
    }

    public Patch fromAvro(GamePatchedEvent event, com.model.Game game) {
        Patch patch = new Patch();
        patch.setVersion(event.getVersion());
        patch.setDescription(event.getChangelog());
        patch.setReleaseDate(String.valueOf(event.getReleaseTimestamp()));
        patch.setGame(game); // Game récupéré en base via event.getGameId()
        return patch;
    }
}
