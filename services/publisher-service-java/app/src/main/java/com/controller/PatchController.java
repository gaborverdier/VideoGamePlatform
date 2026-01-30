package com.controller;

import com.gaming.api.models.PatchModel;
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
@RequestMapping("/api/patch")
public class PatchController {
    @Autowired
    private PatchService patchService;
    @Autowired
    private PatchMapper patchMapper;
    @Autowired
    private GameRepository gameRepository;

    @GetMapping
    public ResponseEntity<List<PatchModel>> getAllPatches() {
        return ResponseEntity.ok(patchService.getAllPatches());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatchModel> getPatchById(@PathVariable String id) {
        return patchService.getPatchById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/game/{gameId}")
    public ResponseEntity<List<PatchModel>> getPatchesByGame(@PathVariable String gameId) {
        return ResponseEntity.ok(patchService.getPatchesByGame(gameId));
    }

    @PostMapping("/create")
    public ResponseEntity<PatchModel> createPatch(@RequestBody PatchModel patchModel) {
        Game game = gameRepository.findById(patchModel.getGameId())
            .orElseThrow(() -> new IllegalArgumentException("Jeu introuvable avec l'ID: " + patchModel.getGameId()));
        Patch patch = patchMapper.fromDTO(patchModel, game);
        return ResponseEntity.ok(patchService.createPatch(patch));
    }

}
