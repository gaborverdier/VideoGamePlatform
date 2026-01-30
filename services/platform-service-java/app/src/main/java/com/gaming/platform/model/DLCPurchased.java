package com.gaming.platform.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "dlc-purchased")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DLCPurchased {
    @Id
    private String id;

    @Column(nullable = false)
    private String dlcId;

    @Column(nullable = false)
    private String userId;

}
