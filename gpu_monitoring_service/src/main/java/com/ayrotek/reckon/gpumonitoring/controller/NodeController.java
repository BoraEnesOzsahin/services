package com.ayrotek.reckon.gpumonitoring.controller;

import com.ayrotek.reckon.gpumonitoring.dto.request.HeartbeatRequest;
import com.ayrotek.reckon.gpumonitoring.dto.request.NodeInitializeRequest;
import com.ayrotek.reckon.gpumonitoring.dto.request.PowerAdjustRequest;
import com.ayrotek.reckon.gpumonitoring.dto.response.HeartbeatResponse;
import com.ayrotek.reckon.gpumonitoring.dto.response.NodeResponse;
import com.ayrotek.reckon.gpumonitoring.dto.response.TelemetryResponse;
import com.ayrotek.reckon.gpumonitoring.service.NodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/nodes")
@RequiredArgsConstructor
@Tag(name = "nodes", description = "RECKON GPU node registration, telemetry and power control")
public class NodeController {

    private final NodeService nodeService;

    @Operation(summary = "Register a rig node (INITIALIZING state)",
            description = "Called by the rig client with its hardware_id and GPU inventory. "
                    + "New nodes are stored as PENDING until an admin approves them.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Node approved; returns node_id and api_token"),
            @ApiResponse(responseCode = "202", description = "Registration accepted; waiting for admin approval"),
            @ApiResponse(responseCode = "403", description = "Node has been revoked")
    })
    @PostMapping("/initialize")
    public ResponseEntity<?> initialize(@Valid @RequestBody NodeInitializeRequest request) {
        NodeInitializeRequest processedRequest = new NodeInitializeRequest(
                request.hardwareId(),
                request.model(),
                Optional.ofNullable(request.fwVersion()).orElse("default-fw-version"),
                Optional.ofNullable(request.capabilities()).orElse(new NodeInitializeRequest.Capabilities(1000, 200)),
                request.gpuInventory()
        );
        return nodeService.initialize(processedRequest);
    }

    @Operation(summary = "Telemetry heartbeat (RUNNING state)",
            description = "Called periodically by the rig client with GPU telemetry. "
                    + "Returns a pending adjust_power command when one is queued for the node.",
            security = @SecurityRequirement(name = "node-token"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Telemetry stored; optional command in response"),
            @ApiResponse(responseCode = "401", description = "Invalid/expired token or node not approved "
                    + "(client wipes secrets and re-registers)")
    })
    @PostMapping("/heartbeat")
    public ResponseEntity<HeartbeatResponse> heartbeat(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @Valid @RequestBody HeartbeatRequest request) {
        return ResponseEntity.ok(nodeService.heartbeat(authorization, request));
    }

    // --- Admin / management ---

    @Operation(summary = "List all nodes with online status")
    @GetMapping
    public ResponseEntity<List<NodeResponse>> getNodes() {
        return ResponseEntity.ok(nodeService.getNodes());
    }

    @Operation(summary = "Get node details including GPU inventory")
    @GetMapping("/{nodeId}")
    public ResponseEntity<NodeResponse> getNode(@PathVariable String nodeId) {
        return ResponseEntity.ok(nodeService.getNode(nodeId));
    }

    @Operation(summary = "Approve a pending node",
            description = "After approval the rig client receives its api_token on the next initialize retry.")
    @PostMapping("/{nodeId}/approve")
    public ResponseEntity<NodeResponse> approve(@PathVariable String nodeId) {
        return ResponseEntity.ok(nodeService.approve(nodeId));
    }

    @Operation(summary = "Revoke a node",
            description = "Heartbeats start returning 401 and re-registration is rejected with 403.")
    @PostMapping("/{nodeId}/revoke")
    public ResponseEntity<NodeResponse> revoke(@PathVariable String nodeId) {
        return ResponseEntity.ok(nodeService.revoke(nodeId));
    }

    @Operation(summary = "Queue a power setpoint command",
            description = "The total watt target is delivered to the rig on its next heartbeat; "
                    + "the client distributes it per GPU and applies it via rocm-smi.")
    @PostMapping("/{nodeId}/power")
    public ResponseEntity<Void> requestPowerAdjust(@PathVariable String nodeId,
                                                   @Valid @RequestBody PowerAdjustRequest request) {
        nodeService.requestPowerAdjust(nodeId, request);
        return ResponseEntity.accepted().build();
    }

    @Operation(summary = "Get recent telemetry for a node")
    @GetMapping("/{nodeId}/telemetry")
    public ResponseEntity<List<TelemetryResponse>> getTelemetry(
            @PathVariable String nodeId,
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(nodeService.getTelemetry(nodeId, Math.min(limit, 500)));
    }
}
