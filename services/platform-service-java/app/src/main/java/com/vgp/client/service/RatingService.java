package com.vgp.client.service;

import com.gaming.events.GameRatingSubmittedEvent;
import com.vgp.client.dto.request.RatingRequest;
import com.vgp.client.dto.response.ApiResponse;
import com.vgp.client.kafka.producer.RatingSubmittedProducer;
import com.vgp.shared.entity.Game;
import com.vgp.shared.entity.User;
import com.vgp.shared.repository.GameRepository;
import com.vgp.shared.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class RatingService {
    
    private static final Logger logger = LoggerFactory.getLogger(RatingService.class);
    
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final RatingSubmittedProducer ratingSubmittedProducer;
    
    public RatingService(
            GameRepository gameRepository,
            UserRepository userRepository,
            RatingSubmittedProducer ratingSubmittedProducer) {
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.ratingSubmittedProducer = ratingSubmittedProducer;
    }
    
    public ApiResponse submitRating(RatingRequest request) {
        try {
            logger.info("Submitting rating for user {} and game {}", request.getUserId(), request.getGameId());
            
            // Validate rating value
            if (request.getRating() < 1 || request.getRating() > 5) {
                throw new IllegalArgumentException("Rating must be between 1 and 5");
            }
            
            // Validate game exists
            Game game = gameRepository.findById(request.getGameId())
                    .orElseThrow(() -> new IllegalArgumentException("Game not found with id: " + request.getGameId()));
            
            // Validate user exists
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + request.getUserId()));
            
            // Generate unique rating ID
            String ratingId = UUID.randomUUID().toString();
            
            // Build and publish Kafka event
            GameRatingSubmittedEvent event = GameRatingSubmittedEvent.newBuilder()
                    .setRatingId(ratingId)
                    .setUserId(user.getId().toString())
                    .setGameId(game.getId().toString())
                    .setRating(request.getRating())
                    .setReview(request.getReview())
                    .setSubmittedTimestamp(Instant.now().toEpochMilli())
                    .build();
            
            ratingSubmittedProducer.publish(event);
            logger.info("GameRatingSubmittedEvent published with rating id: {}", ratingId);
            
            return new ApiResponse(true, "Rating submitted successfully", ratingId);
            
        } catch (IllegalArgumentException e) {
            logger.error("Validation error: {}", e.getMessage());
            return new ApiResponse(false, e.getMessage());
        } catch (Exception e) {
            logger.error("Error submitting rating: {}", e.getMessage(), e);
            return new ApiResponse(false, "Failed to submit rating: " + e.getMessage());
        }
    }
}
