package com.gaming.platform.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.query.Param;

import com.gaming.platform.model.Library;

@Repository
public interface LibraryRepository extends JpaRepository<Library, String> {
    List<Library> findByUserId(String userId);

    List<Library> findByGameId(String gameId);

    boolean existsByUserIdAndGameId(String userId, String gameId);

    Library findByUserIdAndGameId(String userId, String gameId);

    @Modifying
    @Transactional
    @Query("delete from Wishlist w where w.userId = :userId and w.gameId = :gameId")
    void deleteFromWishlistIfExists(@Param("userId") String userId, @Param("gameId") String gameId);
}