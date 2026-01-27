package com.controller;

import com.gaming.api.models.CrashModel;
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
    private CrashMapper crashMapper;


    @GetMapping
    public ResponseEntity<List<CrashModel>> getAllCrashes() {
        List<CrashModel> crashes = crashMapper.toDTOList(crashService.getAllCrashes());
        return ResponseEntity.ok(crashes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CrashModel> getCrashById(@PathVariable String id) {
        Optional<Crash> crash = crashService.getCrashById(id);
        if (crash.isPresent()) {
            return ResponseEntity.ok(crashMapper.toDTO(crash.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/game/{gameId}")
    public ResponseEntity<List<CrashModel>> getCrashesByGame(@PathVariable String gameId) {
        List<Crash> crashes = crashService.getCrashesByGame(gameId);
        return ResponseEntity.ok(crashMapper.toDTOList(crashes));
    }

    @PostMapping("/report")
    public ResponseEntity<CrashModel> createCrash(@RequestBody CrashModel event) {
        Crash crash = crashService.createCrash(crashMapper.fromDTO(event));
        return ResponseEntity.ok(crashMapper.toDTO(crash));
    }
}
