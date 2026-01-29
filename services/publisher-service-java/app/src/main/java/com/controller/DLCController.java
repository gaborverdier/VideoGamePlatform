package com.controller;

import com.gaming.api.models.DLCModel;
import com.mapper.DLCMapper;
import com.model.DLC;
import com.service.DLCService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dlc")
public class DLCController {
    @Autowired
    private DLCService dlcService;
    @Autowired
    private DLCMapper dlcMapper;

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

    @PostMapping("/create")
    public ResponseEntity<DLCModel> createDLC(@RequestBody DLCModel dlcModel) {
        
        DLC dlc = dlcMapper.fromDTO(dlcModel);
        return dlcService.createDLC(dlc)
                .map(ResponseEntity::ok)
                .orElseThrow(()-> new RuntimeException("Erreur lors de la création du DLC"));
    }

    @PutMapping("/update")
    public ResponseEntity<DLCModel> updateDLC(@RequestBody DLCModel dlcModel) {
        
        DLC DLC = dlcMapper.fromDTO(dlcModel);
        return dlcService.updateDLC(DLC)
                .map(ResponseEntity::ok)
                .orElseThrow(()-> new RuntimeException("Erreur lors de la mise à jour du DLC"));
        
        
    }

}
