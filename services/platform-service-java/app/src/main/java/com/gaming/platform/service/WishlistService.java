package com.gaming.platform.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gaming.api.models.GameModel;
import com.gaming.api.models.NotificationModel;
import com.gaming.api.models.WishlistModel;
import com.gaming.events.GameReviewed;
import com.gaming.platform.model.Wishlist;
import com.gaming.platform.producer.EventProducer;
import com.gaming.platform.repository.WishlistRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class WishlistService {
    private final WishlistRepository wishlistRepository;
    private final GameService gameService;
    private final EventProducer kafkaProducerService;
    private final NotificationsService notificationsService;

    public List<GameModel> getUserWishlist(String userId) {
        List<GameModel> games = wishlistRepository.findByUserId(userId).stream()
                .map(w -> gameService.getGameById(w.getGameId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        log.info("Retrieved {} games from wishlist for user {}", games.size(), userId);
        return games;
    }

    public Wishlist addToWishlist(WishlistModel newEntry) {
        // check if the item is already in the wishlist could be added here
        List<Wishlist> existingItems = wishlistRepository.findByUserIdAndGameId(newEntry.getUserId(),
                newEntry.getGameId());
        if (!existingItems.isEmpty()) {
            log.info("Game {} is already in wishlist for user {}", newEntry.getGameId(), newEntry.getUserId());
            return existingItems.get(0);
        }

        Wishlist wishlist = new Wishlist();
        wishlist.setUserId(newEntry.getUserId());
        wishlist.setGameId(newEntry.getGameId());
        wishlist.setAddedAt(Instant.ofEpochMilli(newEntry.getAddedAt()));
        Wishlist savedWishlist = wishlistRepository.save(wishlist);
        log.info("Added game {} to wishlist for user {}", newEntry.getGameId(), newEntry.getUserId());
        return savedWishlist;
    }

    @Transactional
    public boolean removeFromWishlist(String userId, String gameId) {
        List<Wishlist> existing = wishlistRepository.findByUserIdAndGameId(userId, gameId);
        if (existing.isEmpty()) {
            log.info("No wishlist entry for user {} and game {}", userId, gameId);
            return false;
        }
        try {
            wishlistRepository.deleteByUserIdAndGameId(userId, gameId);
            log.info("Removed game {} from wishlist for user {}", gameId, userId);
            return true;
        } catch (Exception ex) {
            log.error("Failed to remove wishlist entry for user {} game {}: {}", userId, gameId, ex.getMessage(), ex);
            return false;
        }
    }

    public void createNotificationFromEvent(GameReviewed event) {
        NotificationModel notification = NotificationModel.newBuilder()
                .setNotificationId(UUID.randomUUID().toString())
                .setUserId(event.getUserId())
                .setDescription("Thank you " + event.getUsername() + " for reviewing the game!")
                .setDate(Instant.now().toEpochMilli())
                .build();
        // send notification event via Kafka
        kafkaProducerService.publishNotification(notification);
        // also create notification in the database
        notificationsService.createNotification(notification.getUserId(), notification.getDescription());

        log.info("Created notification for user {}: {}", event.getUserId(), notification.getDescription());

    }

    public void notifyWishlistUsersOfNewReview(String gameId, String gameTitle) {
        List<Wishlist> wishlists = wishlistRepository.findByGameId(gameId);
        for (Wishlist wishlist : wishlists) {
            notificationsService.createNotification(
                wishlist.getUserId(), 
                "NEW_REVIEW", 
                gameId, 
                gameTitle, 
                "Nouvel avis disponible", 
                "Un nouvel avis a été posté"
            );
            log.info("Created NEW_REVIEW notification for user {} about game {}", wishlist.getUserId(), gameTitle);
        }
    }

}
