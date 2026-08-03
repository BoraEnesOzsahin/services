package com.ayrotek.reckon.hiveosintegration.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * An algorithm descriptor from HiveOS GET /hive/algos. Used by the client to help the
 * user narrow coin/miner combinations in the mining strategy builder.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record HiveosAlgo(
        String algo,
        String name
) {
}
