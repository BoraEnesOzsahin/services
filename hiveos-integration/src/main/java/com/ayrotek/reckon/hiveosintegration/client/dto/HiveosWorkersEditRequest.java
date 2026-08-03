package com.ayrotek.reckon.hiveosintegration.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record HiveosWorkersEditRequest(
        @JsonProperty("worker_ids") List<Long> workerIds,
        Map<String, Object> data
) {
}
