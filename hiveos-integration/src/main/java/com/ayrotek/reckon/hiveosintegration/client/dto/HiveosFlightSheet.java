package com.ayrotek.reckon.hiveosintegration.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HiveosFlightSheet(
        Long id,
        String name,
        List<HiveosFsItem> items,
        @JsonProperty("workers_count") Integer workersCount
) {
}
