package com.ayrotek.reckon.hiveosintegration.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HiveosListResponse<T>(
        List<T> data
) {
}
