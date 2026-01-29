package com.gaming.platform.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gaming.platform.model.DLCPurchased;
import com.gaming.platform.repository.DLCPurchasedRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DLCPurchasedService {

    private final DLCPurchasedRepository dlcPurchasedRepository;

    
    @Transactional
    public DLCPurchased recordDLCPurchase(String dlcId, String userId) {
        // Implementation to record DLC purchase
        log.info("Recording purchase of DLC {} by user {}", dlcId, userId);

        // TODO: Add actual implementation to update balance


        DLCPurchased dlcPurchased = new DLCPurchased();
        dlcPurchased.setDlcId(dlcId);
        dlcPurchased.setUserId(userId);
        dlcPurchasedRepository.save(dlcPurchased);

        log.info("Recorded purchase of DLC {} by user {}", dlcId, userId);
        return dlcPurchased;
    }

    public List<DLCPurchased> getDLCPurchasesByUser(String userId) {
        log.info("Fetching DLC purchases for user {}", userId);
        return dlcPurchasedRepository.findByUserId(userId);

    }
    
}
