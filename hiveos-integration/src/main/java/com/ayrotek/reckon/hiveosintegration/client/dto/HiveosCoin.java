package com.ayrotek.reckon.hiveosintegration.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * A coin descriptor from HiveOS GET /hive/coins. Used by the client to populate the
 * coin dropdown of the mining strategy builder.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record HiveosCoin(
        Long id,
        String coin,
        String name,
        List<String> algos
) {
}
