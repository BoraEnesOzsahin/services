package com.ayrotek.reckon.hiveosintegration.dto;

import java.util.List;

/**
 * @param workerIds target workers; when omitted the miner is stopped on ALL farm workers
 */
public record StopMiningRequest(
        List<Long> workerIds
) {
}
