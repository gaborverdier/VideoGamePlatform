package com.controller;

import com.gaming.api.dto.DLCDTO;
import com.mapper.DLCMapper;
import com.model.DLC;
import com.model.Game;
import com.repository.GameRepository;
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
    @Autowired
    private GameRepository gameRepository;

    @GetMapping
    public ResponseEntity<List<DLCDTO>> getAllDLCs() {
        return ResponseEntity.ok(dlcService.getAllDLCs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DLCDTO> getDLCById(@PathVariable Long id) {
        return dlcService.getDLCById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/game/{gameId}")
    public ResponseEntity<List<DLCDTO>> getDLCsByGame(@PathVariable Long gameId) {
        return ResponseEntity.ok(dlcService.getDLCsByGame(gameId));
    }

    @PostMapping("/game/{gameId}")
    public ResponseEntity<DLCDTO> createDLC(@PathVariable Long gameId, @RequestBody DLCDTO dlcDTO) {
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Jeu introuvable avec l'ID: " + gameId));
        DLC dlc = DLCMapper.fromDTO(dlcDTO, game);
        return ResponseEntity.ok(dlcService.createDLC(gameId, dlc));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DLCDTO> updateDLC(@PathVariable Long id, @RequestBody DLCDTO dlcDTO) {
        DLCDTO existingDLC = dlcService.getDLCById(id)
            .orElseThrow(() -> new IllegalArgumentException("DLC introuvable avec l'ID: " + id));
        Game game = gameRepository.findById(existingDLC.getGameId())
            .orElseThrow(() -> new IllegalArgumentException("Jeu introuvable"));
        DLC dlc = DLCMapper.fromDTO(dlcDTO, game);
        dlc.setId(id);
        return ResponseEntity.ok(dlcService.updateDLC(id, dlc));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDLC(@PathVariable Long id) {
        dlcService.deleteDLC(id);
        return ResponseEntity.noContent().build();
    }
}
