package com.controller;

import com.gaming.api.dto.CrashDTO;
import com.mapper.CrashMapper;
import com.service.CrashService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import com.model.Crash;


@RestController
@RequestMapping("api/crash")
public class CrashController {
    @Autowired
    private CrashService crashService;
    @Autowired
    private CrashMapper CrashMapper;


    @GetMapping
    public ResponseEntity<List<CrashDTO>> getAllCrashes() {
        List<CrashDTO> crashes = CrashMapper.toDTOList(crashService.getAllCrashes());
        return ResponseEntity.ok(crashes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CrashDTO> getCrashById(@PathVariable Long id) {
        Optional<Crash> crash = crashService.getCrashById(id);
        if (crash.isPresent()) {
            return ResponseEntity.ok(CrashMapper.toDTO(crash.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/game/{gameId}")
    public ResponseEntity<List<CrashDTO>> getCrashesByGame(@PathVariable Long gameId) {
        List<Crash> crashes = crashService.getCrashesByGame(gameId);
        return ResponseEntity.ok(CrashMapper.toDTOList(crashes));
    }

    @PostMapping("/report")
    public ResponseEntity<CrashDTO> createCrash(@RequestBody CrashDTO event) {
        Crash crash = crashService.createCrash(CrashMapper.fromDTO(event));
        return ResponseEntity.ok(CrashMapper.toDTO(crash));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CrashDTO> updateCrash(@PathVariable Long id, @RequestBody CrashDTO event) {
        Crash updated = crashService.updateCrash(id, CrashMapper.fromDTO(event));
        return ResponseEntity.ok(CrashMapper.toDTO(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCrash(@PathVariable Long id) {
        crashService.deleteCrash(id);
        return ResponseEntity.noContent().build();
    }
}
