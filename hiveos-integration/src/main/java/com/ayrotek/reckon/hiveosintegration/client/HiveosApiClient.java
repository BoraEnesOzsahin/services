package com.ayrotek.reckon.hiveosintegration.client;

import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosAlgo;
import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosCoin;
import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosCommandRequest;
import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosFarm;
import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosMiner;
import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosFlightSheet;
import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosFlightSheetCreateRequest;
import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosListResponse;
import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosPoolTemplate;
import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosWallet;
import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosWalletCreateRequest;
import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosWorker;
import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosWorkersCommandRequest;
import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosWorkersEditRequest;
import com.ayrotek.reckon.hiveosintegration.config.HiveosFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(
        name = "hiveos-api",
        url = "${hiveos.api.base-url}",
        configuration = HiveosFeignConfig.class
)
public interface HiveosApiClient {

    // --- Farms ---

    @GetMapping("/farms")
    HiveosListResponse<HiveosFarm> getFarms();

    @GetMapping("/farms/{farmId}")
    HiveosFarm getFarm(@PathVariable("farmId") Long farmId);

    // --- Workers ---

    @GetMapping("/farms/{farmId}/workers")
    HiveosListResponse<HiveosWorker> getWorkers(@PathVariable("farmId") Long farmId);

    @GetMapping("/farms/{farmId}/workers/{workerId}")
    HiveosWorker getWorker(@PathVariable("farmId") Long farmId, @PathVariable("workerId") Long workerId);

    @PatchMapping("/farms/{farmId}/workers")
    void editWorkers(@PathVariable("farmId") Long farmId, @RequestBody HiveosWorkersEditRequest request);

    @PostMapping("/farms/{farmId}/workers/command")
    void executeWorkersCommand(@PathVariable("farmId") Long farmId,
                               @RequestBody HiveosWorkersCommandRequest request);

    @PostMapping("/farms/{farmId}/workers/{workerId}/command")
    void executeCommand(@PathVariable("farmId") Long farmId,
                        @PathVariable("workerId") Long workerId,
                        @RequestBody HiveosCommandRequest request);

    // --- Wallets ---

    @GetMapping("/farms/{farmId}/wallets")
    HiveosListResponse<HiveosWallet> getWallets(@PathVariable("farmId") Long farmId);

    @GetMapping("/farms/{farmId}/wallets/{walletId}")
    HiveosWallet getWallet(@PathVariable("farmId") Long farmId, @PathVariable("walletId") Long walletId);

    @PostMapping("/farms/{farmId}/wallets")
    HiveosWallet createWallet(@PathVariable("farmId") Long farmId, @RequestBody HiveosWalletCreateRequest request);

    @DeleteMapping("/farms/{farmId}/wallets/{walletId}")
    void deleteWallet(@PathVariable("farmId") Long farmId, @PathVariable("walletId") Long walletId);

    // --- Flight sheets ---

    @GetMapping("/farms/{farmId}/fs")
    HiveosListResponse<HiveosFlightSheet> getFlightSheets(@PathVariable("farmId") Long farmId);

    @GetMapping("/farms/{farmId}/fs/{fsId}")
    HiveosFlightSheet getFlightSheet(@PathVariable("farmId") Long farmId, @PathVariable("fsId") Long fsId);

    @PostMapping("/farms/{farmId}/fs")
    HiveosFlightSheet createFlightSheet(@PathVariable("farmId") Long farmId,
                                        @RequestBody HiveosFlightSheetCreateRequest request);

    @PatchMapping("/farms/{farmId}/fs/{fsId}")
    void updateFlightSheet(@PathVariable("farmId") Long farmId,
                           @PathVariable("fsId") Long fsId,
                           @RequestBody HiveosFlightSheetCreateRequest request);

    @DeleteMapping("/farms/{farmId}/fs/{fsId}")
    void deleteFlightSheet(@PathVariable("farmId") Long farmId, @PathVariable("fsId") Long fsId);

    // --- Pools ---

    @GetMapping("/pools/by_coin/{coin}")
    HiveosListResponse<HiveosPoolTemplate> getPoolsByCoin(@PathVariable("coin") String coin);

    // --- Metadata (strategy builder discovery) ---
    // HiveOS returns these /hive/* catalogs wrapped as {data:[...]}.

    @GetMapping("/hive/coins")
    HiveosListResponse<HiveosCoin> getCoins();

    @GetMapping("/hive/miners")
    HiveosListResponse<HiveosMiner> getMiners();

    @GetMapping("/hive/algos")
    HiveosListResponse<HiveosAlgo> getAlgos();
}
