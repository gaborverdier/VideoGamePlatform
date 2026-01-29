package com.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gaming.events.GameCrashReported;
import com.mapper.GameCrashReportedMapper;

@Component
public class CrashReportedEventHandler {

    @Autowired
    private com.service.CrashService crashService;
    @Autowired
    private GameCrashReportedMapper gameCrashReportedMapper;


    public void handle(GameCrashReported crashEvent) {
        crashService.createCrash(gameCrashReportedMapper.fromAvro(crashEvent));
    }
}
