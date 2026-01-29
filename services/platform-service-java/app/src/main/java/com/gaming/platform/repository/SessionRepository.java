package com.gaming.platform.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gaming.platform.model.Session;

@Repository
public interface SessionRepository extends JpaRepository<Session, String> {
    List<Session> findByGameId(String gameId);
    List<Session> findByGameIdAndStartTimestampBetween(String gameId, long start, long end);

    // find sessions that overlap the interval [start, end]
    // i.e. sessions with start <= end AND end >= start
    List<Session> findByGameIdAndStartTimestampLessThanEqualAndEndTimestampGreaterThanEqual(String gameId, long end, long start);
}
