package com.controller;

import com.model.CrashAggregation;
import com.service.CrashService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/crash-aggregations")
public class CrashController {
    
    @Autowired
    private CrashService crashService;

    @GetMapping
    public ResponseEntity<List<CrashAggregation>> getAllCrashAggregations() {
        return ResponseEntity.ok(crashService.getAllCrashes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CrashAggregation> getCrashAggregationById(@PathVariable String id) {
        Optional<CrashAggregation> crash = crashService.getCrashById(id);
        return crash.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/game/{gameId}")
    public ResponseEntity<List<CrashAggregation>> getCrashAggregationsByGame(@PathVariable String gameId) {
        List<CrashAggregation> crashes = crashService.getCrashesByGame(gameId);
        return ResponseEntity.ok(crashes);
    }
}
