package com.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.model.CrashAggregation;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CrashRepository extends JpaRepository<CrashAggregation, String> {
    List<CrashAggregation> findByGameId(String gameId);
    List<CrashAggregation> findByGameIdOrderByWindowStartDesc(String gameId);
}
