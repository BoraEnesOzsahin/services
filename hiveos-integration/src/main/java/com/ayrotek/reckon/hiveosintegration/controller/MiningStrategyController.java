package com.ayrotek.reckon.hiveosintegration.controller;

import com.ayrotek.reckon.hiveosintegration.dto.ActiveMiningStrategyResponse;
import com.ayrotek.reckon.hiveosintegration.dto.MiningStrategyRequest;
import com.ayrotek.reckon.hiveosintegration.dto.MiningStrategyResponse;
import com.ayrotek.reckon.hiveosintegration.dto.StartMiningResponse;
import com.ayrotek.reckon.hiveosintegration.service.MiningStrategyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * CRUD + execution for reusable, user-owned mining strategies. Ownership comes from the
 * gateway-forwarded {@code X-User-Id} header; the client never sends it directly.
 */
@RestController
@RequestMapping("/api/v1/hiveos/strategies")
@RequiredArgsConstructor
@Tag(name = "strategies", description = "Reusable mining strategies built on the client")
public class MiningStrategyController {

    private static final String USER_ID_HEADER = "X-User-Id";

    private final MiningStrategyService strategyService;

    @Operation(summary = "List the current user's mining strategies")
    @GetMapping
    public ResponseEntity<List<MiningStrategyResponse>> list(
            @RequestHeader(value = USER_ID_HEADER, required = false) String userId) {
        return ResponseEntity.ok(strategyService.list(userId));
    }

    @Operation(summary = "Get a mining strategy")
    @GetMapping("/{id}")
    public ResponseEntity<MiningStrategyResponse> get(
            @RequestHeader(value = USER_ID_HEADER, required = false) String userId,
            @PathVariable String id) {
        return ResponseEntity.ok(strategyService.get(userId, id));
    }

    @Operation(summary = "Create a mining strategy")
    @PostMapping
    public ResponseEntity<MiningStrategyResponse> create(
            @RequestHeader(value = USER_ID_HEADER, required = false) String userId,
            @Valid @RequestBody MiningStrategyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(strategyService.create(userId, request));
    }

    @Operation(summary = "Update a mining strategy")
    @PutMapping("/{id}")
    public ResponseEntity<MiningStrategyResponse> update(
            @RequestHeader(value = USER_ID_HEADER, required = false) String userId,
            @PathVariable String id,
            @Valid @RequestBody MiningStrategyRequest request) {
        return ResponseEntity.ok(strategyService.update(userId, id, request));
    }

    @Operation(summary = "Delete a mining strategy")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @RequestHeader(value = USER_ID_HEADER, required = false) String userId,
            @PathVariable String id) {
        strategyService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Run a saved strategy",
            description = "Rebuilds a mining/start request from the strategy and applies it to its farm.")
    @PostMapping("/{id}/run")
    public ResponseEntity<StartMiningResponse> run(
            @RequestHeader(value = USER_ID_HEADER, required = false) String userId,
            @PathVariable String id) {
        return ResponseEntity.ok(strategyService.run(userId, id));
    }

    @Operation(summary = "List currently active strategy runs for the current user")
    @GetMapping("/active")
    public ResponseEntity<List<ActiveMiningStrategyResponse>> listActive(
            @RequestHeader(value = USER_ID_HEADER, required = false) String userId) {
        return ResponseEntity.ok(strategyService.listActive(userId));
    }

    @Operation(summary = "Clear the active strategy state for a farm")
    @DeleteMapping("/active/{farmId}")
    public ResponseEntity<Void> clearActive(
            @RequestHeader(value = USER_ID_HEADER, required = false) String userId,
            @PathVariable Long farmId) {
        strategyService.clearActive(userId, farmId);
        return ResponseEntity.noContent().build();
    }
}
