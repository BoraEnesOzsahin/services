package com.ayrotek.reckon.hiveosintegration.service;

import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosCommandRequest;
import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosFarm;
import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosFlightSheet;
import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosFlightSheetCreateRequest;
import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosWallet;
import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosWalletCreateRequest;
import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosWorker;

import java.util.List;

public interface HiveosService {

    List<HiveosFarm> getFarms();

    HiveosFarm getFarm(Long farmId);

    List<HiveosWorker> getWorkers(Long farmId);

    HiveosWorker getWorker(Long farmId, Long workerId);

    void executeCommand(Long farmId, Long workerId, HiveosCommandRequest request);

    // --- Flight sheets ---

    List<HiveosFlightSheet> getFlightSheets(Long farmId);

    HiveosFlightSheet getFlightSheet(Long farmId, Long fsId);

    HiveosFlightSheet createFlightSheet(Long farmId, HiveosFlightSheetCreateRequest request);

    void updateFlightSheet(Long farmId, Long fsId, HiveosFlightSheetCreateRequest request);

    void deleteFlightSheet(Long farmId, Long fsId);

    // --- Wallets ---

    List<HiveosWallet> getWallets(Long farmId);

    HiveosWallet getWallet(Long farmId, Long walletId);

    HiveosWallet createWallet(Long farmId, HiveosWalletCreateRequest request);

    void deleteWallet(Long farmId, Long walletId);
}
