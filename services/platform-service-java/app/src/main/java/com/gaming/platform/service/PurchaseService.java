package com.gaming.platform.service;

import java.time. LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gaming.api.models.PurchaseModel; 
import com.gaming.api.requests.PurchaseGameRequest;
import com.gaming. platform.model.Game;
import com.gaming.platform.model.Purchase;
import com.gaming.platform.model.User;
import com.gaming.platform.producer.EventProducer;
import com.gaming.platform.repository. GameRepository;
import com.gaming.platform.repository.PurchaseRepository;
import com.gaming. platform.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern. slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final EventProducer eventProducer;

    @Transactional
    public PurchaseModel purchaseGame(PurchaseGameRequest request) {
        
        // Validate user exists
        User user = userRepository.findById(request.getUserId().toString())
                .orElseThrow(() -> new IllegalArgumentException("User not found:  " + request.getUserId()));

        // Validate game exists and is available
        Game game = gameRepository.findById(request.getGameId().toString())
                .orElseThrow(() -> new IllegalArgumentException("Game not found: " + request.getGameId()));

                // Check if user already owns the game
        if (purchaseRepository.existsByUserIdAndGameId(
                request.getUserId().toString(), 
                request.getGameId().toString())) {
            throw new IllegalStateException("User already owns this game:  " + game.getTitle());
        }
                // Check user has sufficient balance and deduct it
                double price = game.getPrice() != null ? game.getPrice().doubleValue() : 0.0;
                Double balance = user.getBalance();
                if (balance == null || balance < price) {
                        throw new IllegalStateException("Insufficient balance: required=" + price + " available=" + balance);
                }
                user.setBalance(balance - price);
                userRepository.save(user);

                // Create purchase entity (JPA)
        Purchase purchase = new Purchase();
        purchase.setUserId(request.getUserId().toString());
        purchase.setGameId(request.getGameId().toString());
        purchase.setPrice(game.getPrice());
        purchase.setPurchaseDate(LocalDateTime.now());
        purchase.setPaymentMethod(
            request.getPaymentMethod() != null 
                ? request. getPaymentMethod().toString() 
                : null
        );
        purchase.setRegion(
            request.getRegion() != null 
                ? request.getRegion().toString() 
                : null
        );

        Purchase savedPurchase = purchaseRepository.save(purchase);

        eventProducer.publishGamePurchased(savedPurchase, user, game);

        log.info("Game purchased:  User={}, Game={}, PurchaseId={}", 
                user.getUsername(), game.getTitle(), savedPurchase.getPurchaseId());

        return toPurchaseResponse(savedPurchase, user, game); 
    }

    public List<PurchaseModel> getUserPurchases(String userId) { 
        
        List<Purchase> purchases = purchaseRepository.findByUserId(userId);
        
        return purchases.stream()
                .map(purchase -> {
                    // Fetch related entities
                    User user = userRepository.findById(purchase.getUserId())
                            .orElse(null);
                    Game game = gameRepository.findById(purchase.getGameId())
                            .orElse(null);
                    
                    return toPurchaseResponse(purchase, user, game);
                })
                .collect(Collectors.toList());
    }
    public List<PurchaseModel> getGamePurchases(String gameId) { 
        
        List<Purchase> purchases = purchaseRepository.findByGameId(gameId);
        
        return purchases.stream()
                .map(purchase -> {
                    User user = userRepository.findById(purchase.getUserId())
                            .orElse(null);
                    Game game = gameRepository. findById(purchase.getGameId())
                            .orElse(null);
                    
                    return toPurchaseResponse(purchase, user, game);
                })
                .collect(Collectors.toList());
    }


    private PurchaseModel toPurchaseResponse(Purchase purchase, User user, Game game) {
        
        return PurchaseModel.newBuilder()
                .setPurchaseId(purchase.getPurchaseId())
                .setUserId(user != null ? user.getUserId() : purchase.getUserId())
                .setGameId(game != null ? game.getGameId() : purchase.getGameId())
                .setGameTitle(game != null ? game.getTitle() : "Unknown Game")
                .setPrice(purchase.getPrice() != null ? purchase.getPrice().doubleValue() : 0.0)
                .setPurchaseTimestamp(
                    purchase.getPurchaseDate() != null 
                        ? purchase.getPurchaseDate().atZone(java.time.ZoneOffset.UTC).toInstant().toEpochMilli()
                        : System.currentTimeMillis()
                )
                .setStatus("COMPLETED")
                .build();
    }
}