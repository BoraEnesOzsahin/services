package com.ayrotek.reckon.hiveosintegration.dto;

import java.util.List;

public record StartMiningResponse(
        Long walletId,
        Long flightSheetId,
        String flightSheetName,
        List<String> poolUrls,
        List<Long> appliedWorkerIds
) {
}
