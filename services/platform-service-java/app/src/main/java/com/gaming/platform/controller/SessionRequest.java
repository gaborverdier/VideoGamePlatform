package com.gaming.platform.controller;

import lombok.Data;

@Data
public class SessionRequest {
    private String gameId;
    private String userId;
    // epoch millis for start
    private long startTimestamp;
    // duration played in millis
    private long timePlayed;
}
