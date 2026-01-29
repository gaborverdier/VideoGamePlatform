package com.gaming.platform.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.gaming.platform.model.Session;
import com.gaming.platform.repository.SessionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SessionService {
    private final SessionRepository sessionRepository;

    public Session createSession(Session session) {
        // compute end timestamp
        session.setEndTimestamp(session.getStartTimestamp() + session.getTimePlayed());
        return sessionRepository.save(session);
    }

    public List<Session> getSessionsByGameId(String gameId) {
        return sessionRepository.findByGameId(gameId);
    }

    public long totalPlayedTimeBetween(String gameId, long fromInclusive, long toInclusive) {
        // fetch sessions that overlap the requested interval
        List<Session> sessions = sessionRepository.findByGameIdAndStartTimestampLessThanEqualAndEndTimestampGreaterThanEqual(gameId, toInclusive, fromInclusive);
        // for each session, count only the overlapping portion: min(end, to) - max(start, from)
        return sessions.stream().mapToLong(s -> {
            long overlapStart = Math.max(s.getStartTimestamp(), fromInclusive);
            long overlapEnd = Math.min(s.getEndTimestamp(), toInclusive);
            long overlap = overlapEnd - overlapStart;
            return overlap > 0 ? overlap : 0L;
        }).sum();
    }

    public long totalPlayedTime(String gameId) {
        List<Session> sessions = sessionRepository.findByGameId(gameId);
        return sessions.stream().mapToLong(Session::getTimePlayed).sum();
    }
}
