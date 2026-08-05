package com.ayrotek.reckon.gpumonitoring.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "nodes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Node {

    @Id
    private String id;

    @Column(name = "hardware_id", nullable = false, unique = true)
    private String hardwareId;

    private String model;

    @Column(name = "fw_version")
    private String fwVersion;

    @Column(name = "max_power_w")
    private Integer maxPowerW;

    @Column(name = "min_power_w")
    private Integer minPowerW;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NodeStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "last_heartbeat_at")
    private Instant lastHeartbeatAt;

    @Builder.Default
    @OneToMany(mappedBy = "node", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NodeGpu> gpus = new ArrayList<>();

    public void replaceGpus(List<NodeGpu> newGpus) {
        gpus.clear();
        newGpus.forEach(gpu -> {
            gpu.setNode(this);
            gpus.add(gpu);
        });
    }
}
