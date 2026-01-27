package com.gaming.platform.controller;

import com.gaming.events.GameReviewed;
import com.gaming.platform.model.Review;
import com.gaming.platform.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<Review> createReview(@RequestBody GameReviewed event) {
        Review saved = reviewService.saveFromEvent(event);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/game/{gameId}")
    public List<GameReviewed> getReviewsByGame(@PathVariable String gameId) {
        return reviewService.getReviewsByGameId(gameId);
    }

    @GetMapping("/user/{userId}")
    public List<GameReviewed> getReviewsByUser(@PathVariable String userId) {
        return reviewService.getReviewsByUserId(userId);
    }
}
