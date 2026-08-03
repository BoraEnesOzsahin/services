package com.ayrotek.reckon.hiveosintegration.client.dto;

import java.util.Map;

public record HiveosCommandRequest(
        String command,
        Map<String, Object> data
) {
}
