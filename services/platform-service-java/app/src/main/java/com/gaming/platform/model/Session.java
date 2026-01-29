package com.gaming.platform.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String sessionId;

    @Column(nullable = false)
    private String gameId;

    @Column(nullable = false)
    private String userId;

    // use longs for date/time logic (epoch millis)
    @Column(nullable = false)
    private long startTimestamp;

    // duration played in milliseconds
    @Column(nullable = false)
    private long timePlayed;

    // computed: end = startTimestamp + timePlayed
    @Column(nullable = false)
    private long endTimestamp;

}
