package com.gaming.platform.repository;

import com.gaming.platform.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, String> {
    List<Review> findByGameId(String gameId);
    List<Review> findByUserId(String userId);
}
