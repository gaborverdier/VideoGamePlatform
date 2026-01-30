package com.gaming.platform.service;

import com.gaming.platform.model.CrashReport;
import com.gaming.platform.repository.CrashReportRepository;
import org.springframework.stereotype.Service;

@Service
public class CrashReportService {

    private final CrashReportRepository crashReportRepository;

    public CrashReportService(CrashReportRepository crashReportRepository) {
        this.crashReportRepository = crashReportRepository;
    }

    public CrashReport save(CrashReport crashReport) {
        return crashReportRepository.save(crashReport);
    }
}
