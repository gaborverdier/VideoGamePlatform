package com.controller;

import com.gaming.api.models.DLCModel;
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
    public ResponseEntity<List<DLCModel>> getAllDLCs() {
        return ResponseEntity.ok(dlcService.getAllDLCs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DLCModel> getDLCById(@PathVariable String id) {
        return dlcService.getDLCById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/game/{gameId}")
    public ResponseEntity<List<DLCModel>> getDLCsByGame(@PathVariable String gameId) {
        return ResponseEntity.ok(dlcService.getDLCsByGame(gameId));
    }

    @PostMapping("/game/{gameId}")
    public ResponseEntity<DLCModel> createDLC(@PathVariable String gameId, @RequestBody DLCModel dlcModel) {
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Jeu introuvable avec l'ID: " + gameId));
        DLC dlc = DLCMapper.fromDTO(dlcModel, game);
        return ResponseEntity.ok(dlcService.createDLC(gameId, dlc));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DLCModel> updateDLC(@PathVariable String id, @RequestBody DLCModel dlcModel) {
        DLCModel dlcDTO = dlcService.getDLCById(id)
            .orElseThrow(() -> new IllegalArgumentException("DLC introuvable avec l'ID: " + id));
        Game game = gameRepository.findById(dlcDTO.getGameId())
            .orElseThrow(() -> new IllegalArgumentException("Jeu introuvable"));
        DLC dlc = DLCMapper.fromDTO(dlcDTO, game);
        dlc.setId(id);
        return ResponseEntity.ok(dlcService.updateDLC(id, dlc));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDLC(@PathVariable String id) {
        dlcService.deleteDLC(id);
        return ResponseEntity.noContent().build();
    }
}
