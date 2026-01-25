package com.controller;

import com.gaming.api.dto.PatchDTO;
import com.mapper.PatchMapper;
import com.model.Patch;
import com.model.Game;
import com.repository.GameRepository;
import com.service.PatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/patches")
public class PatchController {
    @Autowired
    private PatchService patchService;
    @Autowired
    private PatchMapper patchMapper;
    @Autowired
    private GameRepository gameRepository;

    @GetMapping
    public ResponseEntity<List<PatchDTO>> getAllPatches() {
        return ResponseEntity.ok(patchService.getAllPatches());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatchDTO> getPatchById(@PathVariable Long id) {
        return patchService.getPatchById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/game/{gameId}")
    public ResponseEntity<List<PatchDTO>> getPatchesByGame(@PathVariable Long gameId) {
        return ResponseEntity.ok(patchService.getPatchesByGame(gameId));
    }

    @PostMapping("/game/{gameId}")
    public ResponseEntity<PatchDTO> createPatch(@PathVariable Long gameId, @RequestBody PatchDTO patchDTO) {
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Jeu introuvable avec l'ID: " + gameId));
        Patch patch = patchMapper.fromDTO(patchDTO, game);
        return ResponseEntity.ok(patchService.createPatch(gameId, patch));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatchDTO> updatePatch(@PathVariable Long id, @RequestBody PatchDTO patchDTO) {
        PatchDTO existingPatch = patchService.getPatchById(id)
            .orElseThrow(() -> new IllegalArgumentException("Patch introuvable avec l'ID: " + id));
        Game game = gameRepository.findById(existingPatch.getGameId())
            .orElseThrow(() -> new IllegalArgumentException("Jeu introuvable"));
        Patch patch = patchMapper.fromDTO(patchDTO, game);
        patch.setId(id);
        return ResponseEntity.ok(patchService.updatePatch(id, patch));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatch(@PathVariable Long id) {
        patchService.deletePatch(id);
        return ResponseEntity.noContent().build();
    }
}
