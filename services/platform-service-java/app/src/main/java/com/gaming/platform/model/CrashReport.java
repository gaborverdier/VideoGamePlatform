package com.gaming.platform.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "crash_reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrashReport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String crashId;

    @Column
    private String userId;

    @Column
    private String gameId;

    @Column
    private String gameVersion;

    @Column
    private String platform;

    @Column(length = 2000)
    private String errorMessage;

    @Column
    private LocalDateTime crashTimestamp;

    @Column
    private LocalDateTime receivedAt = LocalDateTime.now();
}
