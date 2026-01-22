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
@RequestMapping("api/crash")
public class CrashController {
    @Autowired
    private CrashService crashService;
    @Autowired
    private CrashMapper CrashMapper;


    @GetMapping
    public ResponseEntity<List<GameCrashReportedEvent>> getAllCrashes() {
        List<GameCrashReportedEvent> crashes = CrashMapper.toAvroList(crashService.getAllCrashes());
        return ResponseEntity.ok(crashes);
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
    public ResponseEntity<List<GameCrashReportedEvent>> getCrashesByGame(@PathVariable Long gameId) {
        List<Crash> crashes = crashService.getCrashesByGame(gameId);
        return ResponseEntity.ok(CrashMapper.toAvroList(crashes));
    }

    @PostMapping("/report")
    public ResponseEntity<GameCrashReportedEvent> createCrash(@RequestBody GameCrashReportedEvent event) {
        Crash crash = crashService.createCrash(CrashMapper.fromAvro(event));
        return ResponseEntity.ok(CrashMapper.toAvro(crash));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GameCrashReportedEvent> updateCrash(@PathVariable Long id, @RequestBody GameCrashReportedEvent event) {
        Crash updated = crashService.updateCrash(id, CrashMapper.fromAvro(event));
        return ResponseEntity.ok(CrashMapper.toAvro(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCrash(@PathVariable Long id) {
        crashService.deleteCrash(id);
        return ResponseEntity.noContent().build();
    }
}
