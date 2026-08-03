package com.ayrotek.reckon.hiveosintegration.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HiveosFarm(
        Long id,
        String name,
        String timezone,
        // access role of the token's account on this farm: monitor|tech|rocket|advanced|full (null when owner)
        String role,
        // farm is locked (e.g. overdraft) — sub-resource calls return 403 while locked
        Boolean locked,
        @JsonProperty("twofa_required") Boolean twofaRequired,
        @JsonProperty("trusted") Boolean trusted,
        @JsonProperty("workers_count") Integer workersCount,
        @JsonProperty("rigs_count") Integer rigsCount,
        @JsonProperty("asics_count") Integer asicsCount,
        HiveosFarmStats stats
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record HiveosFarmStats(
            @JsonProperty("workers_total") Integer workersTotal,
            @JsonProperty("workers_online") Integer workersOnline,
            @JsonProperty("workers_offline") Integer workersOffline,
            @JsonProperty("gpus_total") Integer gpusTotal,
            @JsonProperty("gpus_online") Integer gpusOnline,
            @JsonProperty("power_draw") Double powerDraw,
            @JsonProperty("power_cost") Double powerCost,
            Double asr
    ) {
    }
}
