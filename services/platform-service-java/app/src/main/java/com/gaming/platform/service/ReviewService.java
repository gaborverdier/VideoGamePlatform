package com.gaming.platform.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.gaming.api.models.GameModel;
import com.gaming.events.GameReviewed;
import com.gaming.platform.model.Review;
import com.gaming.platform.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final WishlistService wishlistService;
    private final GameService gameService;
    private final com.gaming.platform.producer.EventProducer eventProducer;

    public Review saveFromEvent(GameReviewed event) {
        Review review = new Review();
        review.setGameId(event.getGameId().toString());
        review.setUserId(event.getUserId().toString());
        review.setUsername(event.getUsername());
        review.setRating(event.getRating());
        review.setComment(event.getReviewText().toString());
        review.setReviewedAt(Instant.now());
        Review resp =  reviewRepository.save(review);

        try{
            Optional<GameModel> game = gameService.getGameById(event.getGameId());

            event.setReviewId(resp.getReviewId());
            // Publish GameReviewed event to Kafka
            eventProducer.publishGameReviewed(event);

            wishlistService.createNotificationFromEvent(event);
            wishlistService.notifyWishlistUsersOfNewReview(event.getGameId(), game.map(GameModel::getTitle).orElse("Unknown Game"));
            log.info("Saved review and notified wishlist users for game {}", event.getGameId());
        } catch (Exception e) {
            log.error("Failed to log saved review: {}", e.getMessage());
        }

        return resp;
    }

    public List<GameReviewed> getReviewsByGameId(String gameId) {
        return reviewRepository.findByGameId(gameId).stream()
                .map(this::toGameReviewed)
                .collect(Collectors.toList());

    }

    public List<GameReviewed> getReviewsByUserId(String userId) {
        return reviewRepository.findByUserId(userId).stream()
                .map(this::toGameReviewed)
                .collect(Collectors.toList());
    }


    private GameReviewed toGameReviewed(Review review) {
        return GameReviewed.newBuilder()
                .setReviewId(review.getReviewId())
                .setGameId(review.getGameId())
                .setUserId(review.getUserId())
                .setUsername(review.getUsername())
                .setRating(review.getRating())
                .setReviewText(review.getComment())
                .setRegistrationTimestamp(review.getReviewedAt() != null ? review.getReviewedAt().toEpochMilli() : System.currentTimeMillis())
                .build();
    }
}
