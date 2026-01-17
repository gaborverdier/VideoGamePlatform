package com.gaming.platform.service;

import com.gaming.platform.dto.PurchaseDTO;
import com.gaming.platform.model.Game;
import com.gaming.platform.model.Purchase;
import com.gaming.platform.model.User;
import com.gaming.platform.producer.EventProducer;
import com.gaming.platform.repository.GameRepository;
import com.gaming.platform.repository.PurchaseRepository;
import com.gaming.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final EventProducer eventProducer;

    @Transactional
    public Purchase purchaseGame(PurchaseDTO dto) {
        // Validate user exists
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Validate game exists and is available
        Game game = gameRepository.findById(dto.getGameId())
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        if (!game.getAvailable()) {
            throw new IllegalStateException("Game is not available for purchase");
        }

        // Check if user already owns the game
        if (purchaseRepository.existsByUserIdAndGameId(dto.getUserId(), dto.getGameId())) {
            throw new IllegalStateException("User already owns this game");
        }

        // Create purchase
        Purchase purchase = new Purchase();
        purchase.setUserId(dto.getUserId());
        purchase.setGameId(dto.getGameId());
        purchase.setPrice(game.getPrice());
        purchase.setPurchaseDate(LocalDateTime.now());
        purchase.setPaymentMethod(dto.getPaymentMethod());
        purchase.setRegion(dto.getRegion());

        Purchase savedPurchase = purchaseRepository.save(purchase);

        // Publish game-purchased event
        eventProducer.publishGamePurchased(savedPurchase, user, game);

        log.info("Game purchased: User={}, Game={}", user.getUsername(), game.getTitle());
        return savedPurchase;
    }

    public List<Purchase> getUserPurchases(String userId) {
        return purchaseRepository.findByUserId(userId);
    }

    public List<Purchase> getGamePurchases(String gameId) {
        return purchaseRepository.findByGameId(gameId);
    }
}