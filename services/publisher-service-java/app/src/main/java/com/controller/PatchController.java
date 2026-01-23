package com.controller;

import com.model.Patch;
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

    @GetMapping
    public ResponseEntity<List<Patch>> getAllPatches() {
        List<Patch> patches = patchService.getAllPatches();
        return ResponseEntity.ok(patches);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Patch> getPatchById(@PathVariable Long id) {
        return patchService.getPatchById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/game/{gameId}")
    public ResponseEntity<List<Patch>> getPatchesByGame(@PathVariable Long gameId) {
        List<Patch> patches = patchService.getPatchesByGame(gameId);
        return ResponseEntity.ok(patches);
    }

    @PostMapping("/game/{gameId}")
    public ResponseEntity<Patch> createPatch(@PathVariable Long gameId, @RequestBody Patch patch) {
        Patch created = patchService.createPatch(gameId, patch);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Patch> updatePatch(@PathVariable Long id, @RequestBody Patch patch) {
        Patch updated = patchService.updatePatch(id, patch);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatch(@PathVariable Long id) {
        patchService.deletePatch(id);
        return ResponseEntity.noContent().build();
    }
}
