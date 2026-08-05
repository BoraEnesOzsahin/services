package com.ayrotek.reckon.gpumonitoring.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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
@Table(name = "telemetry_records", indexes = {
        @Index(name = "idx_telemetry_node_time", columnList = "node_id, received_at")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelemetryRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "node_id", nullable = false)
    private String nodeId;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(name = "reported_at")
    private String reportedAt;

    private String status;

    @Column(name = "system_temp_c")
    private Double systemTempC;

    @Builder.Default
    @OneToMany(mappedBy = "record", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GpuTelemetryRecord> gpuSamples = new ArrayList<>();

    public void addGpuSample(GpuTelemetryRecord sample) {
        sample.setRecord(this);
        gpuSamples.add(sample);
    }
}
