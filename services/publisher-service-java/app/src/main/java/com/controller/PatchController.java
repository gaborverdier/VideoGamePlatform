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
    public List<Patch> getAllPatches() {
        return patchService.getAllPatches();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Patch> getPatchById(@PathVariable Long id) {
        return patchService.getPatchById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/game/{gameId}")
    public List<Patch> getPatchesByGame(@PathVariable Long gameId) {
        return patchService.getPatchesByGame(gameId);
    }

    @PostMapping("/game/{gameId}")
    public Patch createPatch(@PathVariable Long gameId, @RequestBody Patch patch) {
        return patchService.createPatch(gameId, patch);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Patch> updatePatch(@PathVariable Long id, @RequestBody Patch patch) {
        Patch updated = patchService.updatePatch(id, patch);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public void deletePatch(@PathVariable Long id) {
        patchService.deletePatch(id);
    }
}
