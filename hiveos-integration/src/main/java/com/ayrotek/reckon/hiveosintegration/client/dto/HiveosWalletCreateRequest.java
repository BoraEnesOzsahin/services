package com.ayrotek.reckon.hiveosintegration.client.dto;

public record HiveosWalletCreateRequest(
        String coin,
        String name,
        String wal
) {
}
