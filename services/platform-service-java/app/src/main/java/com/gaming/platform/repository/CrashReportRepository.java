package com.gaming.platform.repository;

import com.gaming.platform.model.CrashReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CrashReportRepository extends JpaRepository<CrashReport, String> {
}
