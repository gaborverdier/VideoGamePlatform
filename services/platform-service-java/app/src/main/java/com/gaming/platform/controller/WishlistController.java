package com.gaming.platform.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gaming.api.models.GameModel;
import com.gaming.api.models.WishlistModel;
import com.gaming.platform.model.Wishlist;
import com.gaming.platform.service.WishlistService;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {
    private final WishlistService wishlistService;


    @PostMapping("/new")
    public ResponseEntity<Wishlist> addToWishlist(@RequestBody WishlistModel newEntry) {
        Wishlist saved = wishlistService.addToWishlist(newEntry);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<GameModel>> getUserWishlist(@PathVariable String userId) {
       return ResponseEntity.ok(wishlistService.getUserWishlist(userId));
    }
    
    @DeleteMapping("/user/{userId}/game/{gameId}")
    public ResponseEntity<Void> removeFromWishlist(@PathVariable String userId, @PathVariable String gameId) {
        boolean removed = wishlistService.removeFromWishlist(userId, gameId);
        if (removed) return ResponseEntity.ok().build();
        return ResponseEntity.notFound().build();
    }
    
    
}
