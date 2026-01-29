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

import com.gaming.platform.model.DLCPurchased;
import com.gaming.platform.service.DLCPurchasedService;



@RestController
@RequestMapping("/api/dlc-purchased")
public class DLCPurchasedController {

    private DLCPurchasedService dlcService;

    @PostMapping("/purchase/dlc/{dlcId}/user/{userId}")
    public ResponseEntity<DLCPurchased> purchaseDLC(@PathVariable String dlcId, @PathVariable String userId, @RequestBody String entity) {
        if (dlcService.recordDLCPurchase(dlcId, userId) == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DLCPurchased>> getDLCPurchasesByUser(@PathVariable String userId) {
        return ResponseEntity.ok(dlcService.getDLCPurchasesByUser(userId));
    }
    
}
