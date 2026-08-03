package com.ayrotek.reckon.hiveosintegration.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record HiveosFsItem(
        String coin,
        String pool,
        @JsonProperty("pool_ssl") Boolean poolSsl,
        @JsonProperty("pool_urls") List<String> poolUrls,
        @JsonProperty("wal_id") Long walId,
        String miner,
        @JsonProperty("miner_config") Map<String, Object> minerConfig
) {
}
