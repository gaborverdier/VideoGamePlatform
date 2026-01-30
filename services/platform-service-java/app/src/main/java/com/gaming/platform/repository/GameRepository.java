package com.gaming.platform.repository;

import com.gaming.platform.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameRepository extends JpaRepository<Game, String> {
    List<Game> findByGenre(String genre);
    List<Game> findByPlatform(String platform);
    List<Game> findByTitleContainingIgnoreCase(String title);
}