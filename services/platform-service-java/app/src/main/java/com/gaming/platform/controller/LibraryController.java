package com.gaming.platform.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

import com.gaming.api.models.GameModel;
import com.gaming.platform.service.LibraryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/library")
@RequiredArgsConstructor
public class LibraryController {

    private final LibraryService libraryService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<GameModel>> getUserLibrary(@PathVariable String userId) {
        return ResponseEntity.ok(libraryService.getGamesForUser(userId));
    }

    @PutMapping("/user/{userId}/game/{gameId}/install")
    public ResponseEntity<Void> markInstalled(@PathVariable String userId, @PathVariable String gameId) {
        if (libraryService.markInstalledByUserAndGame(userId, gameId) == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user/{userId}/game/{gameId}/purchase")
    public ResponseEntity<Void> addToLibrary(@PathVariable String userId, @PathVariable String gameId) {
        if (libraryService.addToLibrary(userId, gameId) == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
