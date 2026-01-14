package com.vgp.client.service;

import com.gaming.events.GamePurchasedEvent;
import com.vgp.client.dto.request.PurchaseRequest;
import com.vgp.client.dto.response.PurchaseResponse;
import com.vgp.client.kafka.producer.GamePurchasedProducer;
import com.vgp.shared.entity.Game;
import com.vgp.shared.entity.Purchase;
import com.vgp.shared.entity.User;
import com.vgp.shared.repository.GameRepository;
import com.vgp.shared.repository.PurchaseRepository;
import com.vgp.shared.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class PurchaseService {
    
    private static final Logger logger = LoggerFactory.getLogger(PurchaseService.class);
    
    private final PurchaseRepository purchaseRepository;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final GamePurchasedProducer gamePurchasedProducer;
    
    public PurchaseService(
            PurchaseRepository purchaseRepository,
            GameRepository gameRepository,
            UserRepository userRepository,
            GamePurchasedProducer gamePurchasedProducer) {
        this.purchaseRepository = purchaseRepository;
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.gamePurchasedProducer = gamePurchasedProducer;
    }
    
    @Transactional
    public PurchaseResponse processPurchase(PurchaseRequest request) {
        try {
            logger.info("Processing purchase for user {} and game {}", request.getUserId(), request.getGameId());
            
            // Validate game exists
            Game game = gameRepository.findById(request.getGameId())
                    .orElseThrow(() -> new IllegalArgumentException("Game not found with id: " + request.getGameId()));
            
            // Validate user exists
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + request.getUserId()));
            
            // Validate price
            if (request.getPrice() < 0) {
                throw new IllegalArgumentException("Price must not be negative");
            }
            
            // Save purchase to database
            Purchase purchase = new Purchase(
                user,
                game,
                request.getPrice(),
                "USD",
                request.getPlatform(),
                request.getPaymentMethod()
            );
            purchase = purchaseRepository.save(purchase);
            logger.info("Purchase saved with id: {}", purchase.getId());
            
            // Build and publish Kafka event
            GamePurchasedEvent event = GamePurchasedEvent.newBuilder()
                    .setPurchaseId(purchase.getId().toString())
                    .setUserId(user.getId().toString())
                    .setGameId(game.getId().toString())
                    .setGameTitle(game.getName())
                    .setPrice(purchase.getPrice())
                    .setCurrency(purchase.getCurrency())
                    .setPlatform(purchase.getPlatform())
                    .setPurchaseTimestamp(purchase.getPurchaseTimestamp().toEpochMilli())
                    .setPaymentMethod(purchase.getPaymentMethod())
                    .build();
            
            gamePurchasedProducer.publish(event);
            logger.info("GamePurchasedEvent published for purchase id: {}", purchase.getId());
            
            return new PurchaseResponse(
                purchase.getId(),
                "SUCCESS",
                "Purchase completed successfully"
            );
            
        } catch (IllegalArgumentException e) {
            logger.error("Validation error: {}", e.getMessage());
            return new PurchaseResponse(null, "ERROR", e.getMessage());
        } catch (Exception e) {
            logger.error("Error processing purchase: {}", e.getMessage(), e);
            return new PurchaseResponse(null, "ERROR", "Failed to process purchase: " + e.getMessage());
        }
    }
}
