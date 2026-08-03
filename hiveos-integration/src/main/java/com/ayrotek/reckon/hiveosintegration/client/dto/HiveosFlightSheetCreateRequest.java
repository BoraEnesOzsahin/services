package com.ayrotek.reckon.hiveosintegration.client.dto;

import java.util.List;

public record HiveosFlightSheetCreateRequest(
        String name,
        List<HiveosFsItem> items
) {
}
