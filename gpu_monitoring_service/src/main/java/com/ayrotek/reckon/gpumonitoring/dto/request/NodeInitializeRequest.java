package com.ayrotek.reckon.gpumonitoring.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * Payload sent by the rig client during the INITIALIZING state.
 * JSON is snake_case via global property naming strategy.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record NodeInitializeRequest(
        @NotBlank String hardwareId,
        @NotBlank String model,
        String fwVersion,
        Capabilities capabilities,
        @Valid List<GpuInventoryItem> gpuInventory
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Capabilities(
            Integer maxPowerW,
            Integer minPowerW
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GpuInventoryItem(
            @NotBlank String gpuId,
            String name,
            Integer tdpW,
            ComputeCapability computeCapability
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ComputeCapability(
            Double value,
            String unit
    ) {
    }
}
