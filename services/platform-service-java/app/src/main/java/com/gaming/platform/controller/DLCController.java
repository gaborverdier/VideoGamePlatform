package com.gaming.platform.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gaming.api.models.DLCModel;
import com.gaming.platform.service.DLCService;

@RestController
@RequestMapping("/api/dlc")
public class DLCController {
    private final DLCService dlcService;

    public DLCController(DLCService dlcService) {
        this.dlcService = dlcService;
    }

    @GetMapping("/game/{gameId}")
    public ResponseEntity<List<DLCModel>> getAllDLCsForGame(@PathVariable String gameId) {
        return ResponseEntity.ok(dlcService.getDLCsByGameId(gameId));
    }

    @PostMapping("/new")
    public ResponseEntity<DLCModel> createDLC(@RequestBody DLCModel dlcModel) {
        DLCModel createdDLC = dlcService.createDLC(dlcModel);
        return ResponseEntity.ok(createdDLC);
    }
    
}