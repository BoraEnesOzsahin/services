package com.ayrotek.reckon.hiveosintegration.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * A reusable, user-owned mining strategy. Holds every input the client picked in the
 * strategy builder so it can be re-applied to a farm later via the run endpoint, which
 * rebuilds a {@code StartMiningRequest} from these fields.
 */
@Entity
@Table(name = "mining_strategies")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MiningStrategy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /** Owner user id, taken from the gateway-forwarded X-User-Id header. Null until the gateway forwards it. */
    @Column(name = "owner_user_id")
    private String ownerUserId;

    @Column(nullable = false)
    private String name;

    @Column(name = "farm_id", nullable = false)
    private Long farmId;

    @Column(nullable = false)
    private String coin;

    @Column
    private String algo;

    @Column(nullable = false)
    private String pool;

    @Column(name = "wallet_address", nullable = false)
    private String walletAddress;

    @Column(nullable = false)
    private String miner;

    @Column(name = "pool_ssl")
    private Boolean poolSsl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "pool_urls")
    private List<String> poolUrls;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "worker_ids")
    private List<Long> workerIds;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "miner_config")
    private Map<String, Object> minerConfig;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
