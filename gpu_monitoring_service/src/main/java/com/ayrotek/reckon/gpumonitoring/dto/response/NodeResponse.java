package com.ayrotek.reckon.gpumonitoring.dto.response;

import com.ayrotek.reckon.gpumonitoring.entity.NodeStatus;

import java.time.Instant;
import java.util.List;

public record NodeResponse(
        String id,
        String hardwareId,
        String model,
        String fwVersion,
        NodeStatus status,
        boolean online,
        Integer maxPowerW,
        Integer minPowerW,
        Instant createdAt,
        Instant approvedAt,
        Instant lastHeartbeatAt,
        List<GpuResponse> gpus
) {

    public record GpuResponse(
            String gpuId,
            String name,
            Integer tdpW,
            Double computeValue,
            String computeUnit
    ) {
    }
}
