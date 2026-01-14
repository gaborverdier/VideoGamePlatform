package com.vgp.client.controller;

import com.vgp.client.dto.request.RatingRequest;
import com.vgp.client.dto.response.ApiResponse;
import com.vgp.client.service.RatingService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/client/ratings")
public class RatingController {
    
    private static final Logger logger = LoggerFactory.getLogger(RatingController.class);
    
    private final RatingService ratingService;
    
    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse> submitRating(@Valid @RequestBody RatingRequest request) {
        logger.info("POST /api/client/ratings - Submitting rating for user {} and game {}", 
                    request.getUserId(), request.getGameId());
        
        ApiResponse response = ratingService.submitRating(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    @GetMapping("/game/{gameId}")
    public ResponseEntity<ApiResponse> getRatingsForGame(@PathVariable Integer gameId) {
        logger.info("GET /api/client/ratings/game/{} - Fetching ratings for game", gameId);
        // This endpoint would typically return ratings from a database
        // For now, returning a placeholder response
        return ResponseEntity.ok(new ApiResponse(true, "This endpoint will return ratings when ratings are stored"));
    }
}
