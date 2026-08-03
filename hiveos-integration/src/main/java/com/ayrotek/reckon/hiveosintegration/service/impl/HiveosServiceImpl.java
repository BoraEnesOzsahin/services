package com.ayrotek.reckon.hiveosintegration.service.impl;

import com.ayrotek.reckon.hiveosintegration.client.HiveosApiClient;
import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosCommandRequest;
import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosFarm;
import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosFlightSheet;
import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosFlightSheetCreateRequest;
import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosWallet;
import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosWalletCreateRequest;
import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosWorker;
import com.ayrotek.reckon.hiveosintegration.service.HiveosService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HiveosServiceImpl implements HiveosService {

    private final HiveosApiClient hiveosApiClient;

    @Override
    public List<HiveosFarm> getFarms() {
        return hiveosApiClient.getFarms().data();
    }

    @Override
    public HiveosFarm getFarm(Long farmId) {
        return hiveosApiClient.getFarm(farmId);
    }

    @Override
    public List<HiveosWorker> getWorkers(Long farmId) {
        return hiveosApiClient.getWorkers(farmId).data();
    }

    @Override
    public HiveosWorker getWorker(Long farmId, Long workerId) {
        return hiveosApiClient.getWorker(farmId, workerId);
    }

    @Override
    public void executeCommand(Long farmId, Long workerId, HiveosCommandRequest request) {
        hiveosApiClient.executeCommand(farmId, workerId, request);
    }

    // --- Flight sheets ---

    @Override
    public List<HiveosFlightSheet> getFlightSheets(Long farmId) {
        return hiveosApiClient.getFlightSheets(farmId).data();
    }

    @Override
    public HiveosFlightSheet getFlightSheet(Long farmId, Long fsId) {
        return hiveosApiClient.getFlightSheet(farmId, fsId);
    }

    @Override
    public HiveosFlightSheet createFlightSheet(Long farmId, HiveosFlightSheetCreateRequest request) {
        return hiveosApiClient.createFlightSheet(farmId, request);
    }

    @Override
    public void updateFlightSheet(Long farmId, Long fsId, HiveosFlightSheetCreateRequest request) {
        hiveosApiClient.updateFlightSheet(farmId, fsId, request);
    }

    @Override
    public void deleteFlightSheet(Long farmId, Long fsId) {
        hiveosApiClient.deleteFlightSheet(farmId, fsId);
    }

    // --- Wallets ---

    @Override
    public List<HiveosWallet> getWallets(Long farmId) {
        return hiveosApiClient.getWallets(farmId).data();
    }

    @Override
    public HiveosWallet getWallet(Long farmId, Long walletId) {
        return hiveosApiClient.getWallet(farmId, walletId);
    }

    @Override
    public HiveosWallet createWallet(Long farmId, HiveosWalletCreateRequest request) {
        return hiveosApiClient.createWallet(farmId, request);
    }

    @Override
    public void deleteWallet(Long farmId, Long walletId) {
        hiveosApiClient.deleteWallet(farmId, walletId);
    }
}
