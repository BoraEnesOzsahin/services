package com.ayrotek.reckon.gpumonitoring.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PowerAdjustRequest(
        @NotNull @Min(1) Integer setpointPowerW
) {
}
