package com.controller;

import com.gaming.events.GameCrashReportedEvent;
import com.mapper.CrashMapper;
import com.service.CrashService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import com.model.Crash;


@RestController
@RequestMapping("/crashes")
public class CrashController {
    @Autowired
    private CrashService crashService;
    @Autowired
    private CrashMapper CrashMapper;


    @GetMapping
    public List<GameCrashReportedEvent> getAllCrashes() {
        return CrashMapper.toAvroList(crashService.getAllCrashes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GameCrashReportedEvent> getCrashById(@PathVariable Long id) {
        Optional<Crash> crash = crashService.getCrashById(id);
        if (crash.isPresent()) {
            return ResponseEntity.ok(CrashMapper.toAvro(crash.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/game/{gameId}")
    public List<GameCrashReportedEvent> getCrashesByGame(@PathVariable Long gameId) {
        List<Crash> crashes = crashService.getCrashesByGame(gameId);
        return CrashMapper.toAvroList(crashes);
    }

    @PostMapping("/game/{gameId}")
    public GameCrashReportedEvent createCrash(@PathVariable Long gameId, @RequestBody GameCrashReportedEvent event) {
        return CrashMapper.toAvro(crashService.createCrash(gameId, CrashMapper.fromAvro(event)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GameCrashReportedEvent> updateCrash(@PathVariable Long id, @RequestBody GameCrashReportedEvent event) {
        GameCrashReportedEvent updated = CrashMapper.toAvro(crashService.updateCrash(id, CrashMapper.fromAvro(event)));
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public void deleteCrash(@PathVariable Long id) {
        crashService.deleteCrash(id);
    }
}
