package com.gaming.platform.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gaming.platform.model.DLC;

@Repository
public interface DLCRepository extends JpaRepository<DLC, String> {
    List<DLC> findByGameId(String gameId);
}