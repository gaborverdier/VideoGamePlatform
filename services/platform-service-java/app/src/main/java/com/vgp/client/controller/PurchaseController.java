package com.vgp.client.controller;

import com.vgp.client.dto.request.PurchaseRequest;
import com.vgp.client.dto.response.PurchaseResponse;
import com.vgp.client.service.PurchaseService;
import com.vgp.shared.entity.Purchase;
import com.vgp.shared.repository.PurchaseRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/client/purchases")
public class PurchaseController {
    
    private static final Logger logger = LoggerFactory.getLogger(PurchaseController.class);
    
    private final PurchaseService purchaseService;
    private final PurchaseRepository purchaseRepository;
    
    public PurchaseController(PurchaseService purchaseService, PurchaseRepository purchaseRepository) {
        this.purchaseService = purchaseService;
        this.purchaseRepository = purchaseRepository;
    }
    
    @PostMapping
    public ResponseEntity<PurchaseResponse> purchaseGame(@Valid @RequestBody PurchaseRequest request) {
        logger.info("POST /api/client/purchases - Processing purchase for user {} and game {}", 
                    request.getUserId(), request.getGameId());
        
        PurchaseResponse response = purchaseService.processPurchase(request);
        
        if ("SUCCESS".equals(response.getStatus())) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    @GetMapping("/history")
    public ResponseEntity<List<Purchase>> getPurchaseHistory(@RequestParam Integer userId) {
        logger.info("GET /api/client/purchases/history?userId={} - Fetching purchase history", userId);
        List<Purchase> purchases = purchaseRepository.findByUserId(userId);
        return ResponseEntity.ok(purchases);
    }
}
