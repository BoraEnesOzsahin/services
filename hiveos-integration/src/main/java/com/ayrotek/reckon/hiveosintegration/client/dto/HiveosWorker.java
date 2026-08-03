package com.ayrotek.reckon.hiveosintegration.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HiveosWorker(
        Long id,
        @JsonProperty("farm_id") Long farmId,
        String name,
        String description,
        Boolean active,
        Integer platform,
        @JsonProperty("ip_addresses") List<String> ipAddresses,
        HiveosWorkerStats stats
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record HiveosWorkerStats(
            Boolean online,
            @JsonProperty("boot_time") Long bootTime,
            @JsonProperty("stats_time") Long statsTime,
            @JsonProperty("gpus_online") Integer gpusOnline,
            @JsonProperty("gpus_offline") Integer gpusOffline,
            @JsonProperty("power_draw") Double powerDraw,
            Boolean overheated,
            Boolean invalid
    ) {
    }
}
