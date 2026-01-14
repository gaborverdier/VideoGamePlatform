package com.vgp.client.service;

import com.vgp.client.dto.response.GameDto;
import com.vgp.client.dto.response.UserProfileDto;
import com.vgp.shared.entity.Purchase;
import com.vgp.shared.entity.User;
import com.vgp.shared.repository.PurchaseRepository;
import com.vgp.shared.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProfileService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProfileService.class);
    
    private final UserRepository userRepository;
    private final PurchaseRepository purchaseRepository;
    
    public ProfileService(UserRepository userRepository, PurchaseRepository purchaseRepository) {
        this.userRepository = userRepository;
        this.purchaseRepository = purchaseRepository;
    }
    
    public Optional<UserProfileDto> getUserProfile(Integer userId) {
        logger.debug("Fetching profile for user id: {}", userId);
        
        return userRepository.findById(userId).map(user -> {
            List<Purchase> purchases = purchaseRepository.findByUserId(userId);
            
            List<GameDto> purchasedGames = purchases.stream()
                    .map(purchase -> new GameDto(
                        purchase.getGame().getId(),
                        purchase.getGame().getName(),
                        purchase.getGame().getEditor() != null ? purchase.getGame().getEditor().getId() : null,
                        purchase.getGame().getEditor() != null ? purchase.getGame().getEditor().getName() : null
                    ))
                    .collect(Collectors.toList());
            
            return new UserProfileDto(user.getId(), user.getUsername(), purchasedGames);
        });
    }
}
