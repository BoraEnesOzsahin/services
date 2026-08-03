package com.ayrotek.reckon.hiveosintegration.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "active_mining_strategies",
        uniqueConstraints = @UniqueConstraint(name = "uk_active_strategy_owner_farm", columnNames = {"owner_user_id", "farm_id"})
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActiveMiningStrategy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "owner_user_id")
    private String ownerUserId;

    @Column(name = "farm_id", nullable = false)
    private Long farmId;

    @Column(name = "strategy_id", nullable = false)
    private String strategyId;

    @Column(name = "strategy_name", nullable = false)
    private String strategyName;

    @Column(name = "flight_sheet_id", nullable = false)
    private Long flightSheetId;

    @Column(name = "flight_sheet_name", nullable = false)
    private String flightSheetName;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        startedAt = startedAt != null ? startedAt : now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
