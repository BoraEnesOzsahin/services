package com.ayrotek.reckon.gpumonitoring.dto.response;

/**
 * 202 response while the node waits for admin approval.
 * The client saves node_id locally and keeps retrying.
 */
public record NodePendingResponse(
        String nodeId,
        String status
) {
}
