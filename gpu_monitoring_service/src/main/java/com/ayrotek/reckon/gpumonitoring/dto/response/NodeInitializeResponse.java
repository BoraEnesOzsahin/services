package com.ayrotek.reckon.gpumonitoring.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 200 response for an approved node. The client persists node_id and api_token
 * to secrets.json and switches to the RUNNING state.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record NodeInitializeResponse(
        String nodeId,
        String apiToken,
        InitialCommand initialCommand
) {

    public record InitialCommand(
            Integer heartbeatInterval
    ) {
    }
}
