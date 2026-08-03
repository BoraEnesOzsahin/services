package com.ayrotek.reckon.hiveosintegration.controller;

import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosCommandRequest;
import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosFarm;
import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosFlightSheet;
import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosFlightSheetCreateRequest;
import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosWallet;
import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosWalletCreateRequest;
import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosWorker;
import com.ayrotek.reckon.hiveosintegration.service.HiveosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hiveos")
@RequiredArgsConstructor
@Tag(name = "hiveos", description = "HiveOS farm and worker monitoring")
public class HiveosController {

    private final HiveosService hiveosService;

    @Operation(summary = "List accessible HiveOS farms")
    @GetMapping("/farms")
    public ResponseEntity<List<HiveosFarm>> getFarms() {
        return ResponseEntity.ok(hiveosService.getFarms());
    }

    @Operation(summary = "Get farm info with stats")
    @GetMapping("/farms/{farmId}")
    public ResponseEntity<HiveosFarm> getFarm(@PathVariable Long farmId) {
        return ResponseEntity.ok(hiveosService.getFarm(farmId));
    }

    @Operation(summary = "List workers of a farm")
    @GetMapping("/farms/{farmId}/workers")
    public ResponseEntity<List<HiveosWorker>> getWorkers(@PathVariable Long farmId) {
        return ResponseEntity.ok(hiveosService.getWorkers(farmId));
    }

    @Operation(summary = "Get worker info with telemetry stats")
    @GetMapping("/farms/{farmId}/workers/{workerId}")
    public ResponseEntity<HiveosWorker> getWorker(@PathVariable Long farmId, @PathVariable Long workerId) {
        return ResponseEntity.ok(hiveosService.getWorker(farmId, workerId));
    }

    @Operation(summary = "Execute a raw HiveOS command on a worker",
            description = "e.g. {\"command\":\"reboot\"} or {\"command\":\"miner\",\"data\":{\"action\":\"restart\"}}")
    @PostMapping("/farms/{farmId}/workers/{workerId}/command")
    public ResponseEntity<Void> executeCommand(@PathVariable Long farmId,
                                               @PathVariable Long workerId,
                                               @RequestBody HiveosCommandRequest request) {
        hiveosService.executeCommand(farmId, workerId, request);
        return ResponseEntity.accepted().build();
    }

    // --- Flight sheets ---

    @Operation(summary = "List flight sheets of a farm")
    @GetMapping("/farms/{farmId}/fs")
    public ResponseEntity<List<HiveosFlightSheet>> getFlightSheets(@PathVariable Long farmId) {
        return ResponseEntity.ok(hiveosService.getFlightSheets(farmId));
    }

    @Operation(summary = "Get flight sheet details")
    @GetMapping("/farms/{farmId}/fs/{fsId}")
    public ResponseEntity<HiveosFlightSheet> getFlightSheet(@PathVariable Long farmId, @PathVariable Long fsId) {
        return ResponseEntity.ok(hiveosService.getFlightSheet(farmId, fsId));
    }

    @Operation(summary = "Create a flight sheet",
            description = "items[] carries coin, pool, pool_urls, wal_id, miner and miner_config. "
                    + "Apply it to workers via the mining/start endpoint or a worker edit.")
    @PostMapping("/farms/{farmId}/fs")
    public ResponseEntity<HiveosFlightSheet> createFlightSheet(@PathVariable Long farmId,
                                                               @RequestBody HiveosFlightSheetCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(hiveosService.createFlightSheet(farmId, request));
    }

    @Operation(summary = "Update a flight sheet")
    @PatchMapping("/farms/{farmId}/fs/{fsId}")
    public ResponseEntity<Void> updateFlightSheet(@PathVariable Long farmId,
                                                  @PathVariable Long fsId,
                                                  @RequestBody HiveosFlightSheetCreateRequest request) {
        hiveosService.updateFlightSheet(farmId, fsId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete a flight sheet",
            description = "HiveOS rejects the deletion with 409 while the flight sheet is used by workers.")
    @DeleteMapping("/farms/{farmId}/fs/{fsId}")
    public ResponseEntity<Void> deleteFlightSheet(@PathVariable Long farmId, @PathVariable Long fsId) {
        hiveosService.deleteFlightSheet(farmId, fsId);
        return ResponseEntity.noContent().build();
    }

    // --- Wallets ---

    @Operation(summary = "List wallets of a farm")
    @GetMapping("/farms/{farmId}/wallets")
    public ResponseEntity<List<HiveosWallet>> getWallets(@PathVariable Long farmId) {
        return ResponseEntity.ok(hiveosService.getWallets(farmId));
    }

    @Operation(summary = "Get wallet details")
    @GetMapping("/farms/{farmId}/wallets/{walletId}")
    public ResponseEntity<HiveosWallet> getWallet(@PathVariable Long farmId, @PathVariable Long walletId) {
        return ResponseEntity.ok(hiveosService.getWallet(farmId, walletId));
    }

    @Operation(summary = "Create a wallet")
    @PostMapping("/farms/{farmId}/wallets")
    public ResponseEntity<HiveosWallet> createWallet(@PathVariable Long farmId,
                                                     @RequestBody HiveosWalletCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(hiveosService.createWallet(farmId, request));
    }

    @Operation(summary = "Delete a wallet",
            description = "HiveOS rejects the deletion with 409 while the wallet is used by workers.")
    @DeleteMapping("/farms/{farmId}/wallets/{walletId}")
    public ResponseEntity<Void> deleteWallet(@PathVariable Long farmId, @PathVariable Long walletId) {
        hiveosService.deleteWallet(farmId, walletId);
        return ResponseEntity.noContent().build();
    }
}
