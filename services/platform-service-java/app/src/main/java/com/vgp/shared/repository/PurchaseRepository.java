package com.vgp.shared.repository;

import com.vgp.shared.entity.Purchase;
import com.vgp.shared.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Integer> {
    
    @Query("SELECT p FROM Purchase p WHERE p.user.id = :userId ORDER BY p.purchaseTimestamp DESC")
    List<Purchase> findByUserId(@Param("userId") Integer userId);
}
