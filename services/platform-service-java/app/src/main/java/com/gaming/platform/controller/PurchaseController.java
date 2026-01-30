package com.gaming.platform.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gaming.api.models.PurchaseModel;
import com.gaming.api.requests.PurchaseGameRequest;
import com.gaming.platform.service.PurchaseService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    @PostMapping
    public ResponseEntity<PurchaseModel> purchaseGame(@RequestBody PurchaseGameRequest request) {
        PurchaseModel response = purchaseService.purchaseGame(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PurchaseModel>> getUserPurchases(@PathVariable String userId) {
        return ResponseEntity.ok(purchaseService.getUserPurchases(userId));
    }

    @GetMapping("/game/{gameId}")
    public ResponseEntity<List<PurchaseModel>> getGamePurchases(@PathVariable String gameId) {
        return ResponseEntity.ok(purchaseService.getGamePurchases(gameId));
    }
}