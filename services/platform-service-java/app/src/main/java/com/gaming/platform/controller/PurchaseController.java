package com.gaming.platform.controller;

import com.gaming.platform.dto.PurchaseDTO;
import com.gaming.platform. model.Purchase;
import com.gaming.platform.service.PurchaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework. http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
public class PurchaseController {
    
    private final PurchaseService purchaseService;
    
    @PostMapping
    public ResponseEntity<Purchase> purchaseGame(@Valid @RequestBody PurchaseDTO dto) {
        try {
            Purchase purchase = purchaseService.purchaseGame(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(purchase);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Purchase>> getUserPurchases(@PathVariable String userId) {
        return ResponseEntity.ok(purchaseService.getUserPurchases(userId));
    }
    
    @GetMapping("/game/{gameId}")
    public ResponseEntity<List<Purchase>> getGamePurchases(@PathVariable String gameId) {
        return ResponseEntity.ok(purchaseService. getGamePurchases(gameId));
    }
}