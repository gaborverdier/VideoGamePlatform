package com.controller;

import com.gaming.api.models.CrashAggregationModel;
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
    public ResponseEntity<List<CrashAggregationModel>> getAllCrashAggregations() {
        return ResponseEntity.ok(crashService.getAllCrashes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CrashAggregationModel> getCrashAggregationById(@PathVariable String id) {
        Optional<CrashAggregationModel> crash = crashService.getCrashById(id);
        return crash.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/game/{gameId}")
    public ResponseEntity<List<CrashAggregationModel>> getCrashAggregationsByGame(@PathVariable String gameId) {
        List<CrashAggregationModel> crashes = crashService.getCrashesByGame(gameId);
        return ResponseEntity.ok(crashes);
    }

    @GetMapping("/publisher/{publisherId}")
    public ResponseEntity<List<CrashAggregationModel>> getCrashAggregationsByPublisher(@PathVariable String publisherId) {
        List<CrashAggregationModel> crashes = crashService.getCrashesByPublisher(publisherId);
        return ResponseEntity.ok(crashes);
    }
}
