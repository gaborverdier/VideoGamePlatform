package com.gaming.platform.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gaming.platform.model.Session;
import com.gaming.platform.service.SessionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/session")
@RequiredArgsConstructor
public class SessionController {
    private final SessionService sessionService;

    @PostMapping("/new")
    public ResponseEntity<Session> createSession(@RequestBody SessionRequest req) {
        Session s = new Session();
        s.setGameId(req.getGameId());
        s.setUserId(req.getUserId());
        s.setStartTimestamp(req.getStartTimestamp());
        s.setTimePlayed(req.getTimePlayed());
        Session saved = sessionService.createSession(s);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/game/{gameId}")
    public ResponseEntity<List<Session>> getSessionsForGame(@PathVariable String gameId) {
        return ResponseEntity.ok(sessionService.getSessionsByGameId(gameId));
    }

    // example:"http://localhost:8080/api/session/game/game-123/total?from=0&to=9999999999999"
    @GetMapping("/game/{gameId}/total")
    public ResponseEntity<Long> totalPlayedBetween(
            @PathVariable String gameId,
            @RequestParam("from") long from,
            @RequestParam("to") long to) {
        long total = sessionService.totalPlayedTimeBetween(gameId, from, to);
        return ResponseEntity.ok(total);
    }

    @GetMapping("/game/{gameId}/total/all")
    public ResponseEntity<Long> totalPlayed(@PathVariable String gameId) {
        long total = sessionService.totalPlayedTime(gameId);
        return ResponseEntity.ok(total);
    }

}
