package com.ayrotek.reckon.gpumonitoring.dto.response;

import java.time.Instant;
import java.util.List;

public record TelemetryResponse(
        Long id,
        String nodeId,
        Instant receivedAt,
        String status,
        Double systemTempC,
        List<GpuTelemetryResponse> gpuTelemetry
) {

    public record GpuTelemetryResponse(
            String gpuId,
            Double loadPct,
            Double tempC,
            Double powerDrawW
    ) {
    }
}
