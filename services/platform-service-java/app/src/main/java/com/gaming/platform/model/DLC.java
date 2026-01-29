package com.gaming.platform.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "dlc")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DLC {
    @Id
    private String id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String gameId;

    @Column(nullable = false)
    private Long releaseTimeStamp;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

}
