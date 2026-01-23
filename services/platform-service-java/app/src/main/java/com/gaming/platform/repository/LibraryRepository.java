package com.gaming.platform.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gaming.platform.model.Library;

@Repository
public interface LibraryRepository extends JpaRepository<Library, String> {
    List<Library> findByUserId(String userId);

    boolean existsByUserIdAndGameId(String userId, String gameId);

    Library findByUserIdAndGameId(String userId, String gameId);
}