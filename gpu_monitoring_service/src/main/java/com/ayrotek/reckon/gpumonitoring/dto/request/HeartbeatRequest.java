package com.ayrotek.reckon.gpumonitoring.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * Telemetry payload sent by the rig client in the RUNNING state.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record HeartbeatRequest(
        @NotBlank String nodeId,
        String timestamp,
        Metrics metrics,
        List<GpuTelemetryItem> gpuTelemetry
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Metrics(
            String status,
            Double systemTempC
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GpuTelemetryItem(
            String gpuId,
            Double loadPct,
            Double tempC,
            Double powerDrawW
    ) {
    }
}
