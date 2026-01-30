package com.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "crash_aggregations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrashAggregation {

    @Id
    private String id;

    @Column(nullable = false)
    private String gameId;
    
    @Column(nullable = false)
    private Long crashCount;
    
    @Column(nullable = false)
    private Long timestamp;
    
    @Column(nullable = false)
    private Long windowStart;
    
    @Column(nullable = false)
    private Long windowEnd;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gameId", insertable = false, updatable = false)
    private Game game;
}
