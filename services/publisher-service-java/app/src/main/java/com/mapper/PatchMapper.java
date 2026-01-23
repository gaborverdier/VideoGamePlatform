package com.mapper;

import com.model.Patch;
import com.gaming.events.GamePatchedEvent;

import org.springframework.stereotype.Component;

@Component
public class PatchMapper {
    public GamePatchedEvent toAvro(Patch patch) {
        if (patch == null) throw new IllegalArgumentException("Patch ne peut pas être null");
        if (patch.getGame() == null || patch.getGame().getId() == null)
            throw new IllegalArgumentException("Le jeu associé au patch est obligatoire");
        if (patch.getVersion() == null || patch.getVersion().isEmpty())
            throw new IllegalArgumentException("La version du patch est obligatoire");
        GamePatchedEvent event = new GamePatchedEvent();
        event.setGameId(patch.getGame().getId().toString());
        event.setGameTitle(patch.getGame().getTitle() != null ? patch.getGame().getTitle() : "");
        event.setVersion(patch.getVersion());
        event.setPreviousVersion(null);
        event.setChangelog(patch.getDescription() != null ? patch.getDescription() : "");
        event.setPatchSize(0L);
        event.setReleaseTimestamp(System.currentTimeMillis());
        event.setPublisher(patch.getGame().getPublisher() != null ? patch.getGame().getPublisher().getName() : "");
        return event;
    }

    public Patch fromAvro(GamePatchedEvent event, com.model.Game game) {
        if (event == null) throw new IllegalArgumentException("L'événement ne peut pas être null");
        if (game == null) throw new IllegalArgumentException("Le jeu associé est obligatoire");
        if (event.getVersion() == null || event.getVersion().isEmpty())
            throw new IllegalArgumentException("La version du patch est obligatoire");
        Patch patch = new Patch();
        patch.setVersion(event.getVersion());
        patch.setDescription(event.getChangelog());
        patch.setReleaseDate(String.valueOf(event.getReleaseTimestamp()));
        patch.setGame(game);
        return patch;
    }
}
