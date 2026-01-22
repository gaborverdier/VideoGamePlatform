package com.controller;

import com.model.DLC;
import com.service.DLCService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dlcs")
public class DLCController {
    @Autowired
    private DLCService dlcService;

    @GetMapping
    public ResponseEntity<List<DLC>> getAllDLCs() {
        List<DLC> dlcs = dlcService.getAllDLCs();
        return ResponseEntity.ok(dlcs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DLC> getDLCById(@PathVariable Long id) {
        return dlcService.getDLCById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/game/{gameId}")
    public ResponseEntity<List<DLC>> getDLCsByGame(@PathVariable Long gameId) {
        List<DLC> dlcs = dlcService.getDLCsByGame(gameId);
        return ResponseEntity.ok(dlcs);
    }

    @PostMapping("/game/{gameId}")
    public ResponseEntity<DLC> createDLC(@PathVariable Long gameId, @RequestBody DLC dlc) {
        DLC created = dlcService.createDLC(gameId, dlc);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DLC> updateDLC(@PathVariable Long id, @RequestBody DLC dlc) {
        DLC updated = dlcService.updateDLC(id, dlc);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDLC(@PathVariable Long id) {
        dlcService.deleteDLC(id);
        return ResponseEntity.noContent().build();
    }
}
