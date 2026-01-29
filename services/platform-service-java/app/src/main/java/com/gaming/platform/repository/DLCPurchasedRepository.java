package com.gaming.platform.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gaming.platform.model.DLCPurchased;

@Repository
public interface DLCPurchasedRepository extends JpaRepository<DLCPurchased, String> {
    List<DLCPurchased> findByDlcId(String dlcId);
    List<DLCPurchased> findByUserId(String userId);
}