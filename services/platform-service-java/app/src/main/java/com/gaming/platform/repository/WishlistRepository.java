package com.gaming.platform.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gaming.platform.model.Wishlist;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, String> {
    List<Wishlist> findByUserId(String userId);
    List<Wishlist> findByUserIdAndGameId(String userId, String gameId);
    List<Wishlist> findByGameId(String gameId);
    void deleteByUserIdAndGameId(String userId, String gameId);
}
