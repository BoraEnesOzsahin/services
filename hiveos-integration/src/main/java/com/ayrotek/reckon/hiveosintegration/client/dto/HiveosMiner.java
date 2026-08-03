package com.ayrotek.reckon.hiveosintegration.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * A miner descriptor from HiveOS GET /hive/miners. Used by the client to populate the
 * miner dropdown of the mining strategy builder. {@code name} is the id passed as
 * {@code miner} to the mining/start endpoint (e.g. "t-rex", "lolminer").
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record HiveosMiner(
        String id,
        String name
) {
}
