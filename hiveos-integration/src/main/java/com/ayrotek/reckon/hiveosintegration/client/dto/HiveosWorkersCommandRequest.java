package com.ayrotek.reckon.hiveosintegration.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record HiveosWorkersCommandRequest(
        @JsonProperty("worker_ids") List<Long> workerIds,
        HiveosCommandRequest data
) {
}
