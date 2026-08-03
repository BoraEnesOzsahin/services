package com.ayrotek.reckon.hiveosintegration.controller;

import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosAlgo;
import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosCoin;
import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosMiner;
import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosPoolTemplate;
import com.ayrotek.reckon.hiveosintegration.dto.StartMiningRequest;
import com.ayrotek.reckon.hiveosintegration.dto.StartMiningResponse;
import com.ayrotek.reckon.hiveosintegration.dto.StopMiningRequest;
import com.ayrotek.reckon.hiveosintegration.service.MiningService;
import com.ayrotek.reckon.hiveosintegration.service.MiningStrategyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hiveos")
@RequiredArgsConstructor
@Tag(name = "mining", description = "Remote mining control on HiveOS farms")
public class MiningController {

    private static final String USER_ID_HEADER = "X-User-Id";

    private final MiningService miningService;
    private final MiningStrategyService strategyService;

    @Operation(summary = "Start mining on a PoW network",
            description = "Resolves/creates the wallet, resolves pool URLs from HiveOS templates "
                    + "(e.g. f2pool), creates or reuses a flight sheet and applies it to the given "
                    + "workers (all farm workers when worker_ids is omitted).")
    @PostMapping("/farms/{farmId}/mining/start")
    public ResponseEntity<StartMiningResponse> startMining(@PathVariable Long farmId,
                                                           @RequestHeader(value = USER_ID_HEADER, required = false) String userId,
                                                           @Valid @RequestBody StartMiningRequest request) {
        StartMiningResponse response = miningService.startMining(farmId, request);
        strategyService.clearActive(userId, farmId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Stop mining",
            description = "Sends a miner stop command to the given workers (all farm workers when omitted).")
    @PostMapping("/farms/{farmId}/mining/stop")
    public ResponseEntity<Void> stopMining(@PathVariable Long farmId,
                                           @RequestHeader(value = USER_ID_HEADER, required = false) String userId,
                                           @RequestBody(required = false) StopMiningRequest request) {
        miningService.stopMining(farmId, request);
        strategyService.clearActive(userId, farmId);
        return ResponseEntity.accepted().build();
    }

    @Operation(summary = "List HiveOS pool templates for a coin",
            description = "Shows which pools (and their stratum URLs) HiveOS knows for the coin.")
    @GetMapping("/pools/{coin}")
    public ResponseEntity<List<HiveosPoolTemplate>> getPoolsForCoin(@PathVariable String coin) {
        return ResponseEntity.ok(miningService.getPoolsForCoin(coin));
    }

    // --- Strategy builder discovery: catalogs the client uses to populate dropdowns ---

    @Operation(summary = "List mineable coins known to HiveOS",
            description = "Feeds the coin selector of the client-side mining strategy builder.")
    @GetMapping("/coins")
    public ResponseEntity<List<HiveosCoin>> getCoins() {
        return ResponseEntity.ok(miningService.getCoins());
    }

    @Operation(summary = "List miners known to HiveOS",
            description = "Feeds the miner selector; name is passed as 'miner' to mining/start.")
    @GetMapping("/miners")
    public ResponseEntity<List<HiveosMiner>> getMiners() {
        return ResponseEntity.ok(miningService.getMiners());
    }

    @Operation(summary = "List algorithms known to HiveOS")
    @GetMapping("/algos")
    public ResponseEntity<List<HiveosAlgo>> getAlgos() {
        return ResponseEntity.ok(miningService.getAlgos());
    }
}
