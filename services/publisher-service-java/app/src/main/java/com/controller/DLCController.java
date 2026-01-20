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
    public List<DLC> getAllDLCs() {
        return dlcService.getAllDLCs();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DLC> getDLCById(@PathVariable Long id) {
        return dlcService.getDLCById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/game/{gameId}")
    public List<DLC> getDLCsByGame(@PathVariable Long gameId) {
        return dlcService.getDLCsByGame(gameId);
    }

    @PostMapping("/game/{gameId}")
    public DLC createDLC(@PathVariable Long gameId, @RequestBody DLC dlc) {
        return dlcService.createDLC(gameId, dlc);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DLC> updateDLC(@PathVariable Long id, @RequestBody DLC dlc) {
        DLC updated = dlcService.updateDLC(id, dlc);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public void deleteDLC(@PathVariable Long id) {
        dlcService.deleteDLC(id);
    }
}
