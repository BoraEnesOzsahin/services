package com.ayrotek.reckon.gpumonitoring.service.impl;

import com.ayrotek.reckon.gpumonitoring.dto.request.HeartbeatRequest;
import com.ayrotek.reckon.gpumonitoring.dto.request.NodeInitializeRequest;
import com.ayrotek.reckon.gpumonitoring.dto.request.PowerAdjustRequest;
import com.ayrotek.reckon.gpumonitoring.dto.response.HeartbeatResponse;
import com.ayrotek.reckon.gpumonitoring.dto.response.NodeInitializeResponse;
import com.ayrotek.reckon.gpumonitoring.dto.response.NodePendingResponse;
import com.ayrotek.reckon.gpumonitoring.dto.response.NodeResponse;
import com.ayrotek.reckon.gpumonitoring.dto.response.TelemetryResponse;
import com.ayrotek.reckon.gpumonitoring.entity.GpuTelemetryRecord;
import com.ayrotek.reckon.gpumonitoring.entity.Node;
import com.ayrotek.reckon.gpumonitoring.entity.NodeGpu;
import com.ayrotek.reckon.gpumonitoring.entity.NodeStatus;
import com.ayrotek.reckon.gpumonitoring.entity.PowerCommand;
import com.ayrotek.reckon.gpumonitoring.entity.TelemetryRecord;
import com.ayrotek.reckon.gpumonitoring.exception.InvalidNodeTokenException;
import com.ayrotek.reckon.gpumonitoring.exception.NodeNotFoundException;
import com.ayrotek.reckon.gpumonitoring.exception.NodeRevokedException;
import com.ayrotek.reckon.gpumonitoring.repository.NodeRepository;
import com.ayrotek.reckon.gpumonitoring.repository.PowerCommandRepository;
import com.ayrotek.reckon.gpumonitoring.repository.TelemetryRecordRepository;
import com.ayrotek.reckon.gpumonitoring.security.NodeTokenService;
import com.ayrotek.reckon.gpumonitoring.service.NodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NodeServiceImpl implements NodeService {

    private final NodeRepository nodeRepository;
    private final TelemetryRecordRepository telemetryRecordRepository;
    private final PowerCommandRepository powerCommandRepository;
    private final NodeTokenService nodeTokenService;
    private final RestTemplate restTemplate;

    @Value("${gpu-monitoring.heartbeat-interval}")
    private int heartbeatIntervalSeconds;

    @Override
    @Transactional
    public ResponseEntity<?> initialize(NodeInitializeRequest request) {
        String url = "http://147.228.93.216/api/v1/nodes/initialize";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<NodeInitializeRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            log.info("Request forwarded to {}: {}", url, response.getBody());
        } catch (Exception e) {
            log.error("Failed to forward request to {}", url, e);
        }

        Node node = nodeRepository.findByHardwareId(request.hardwareId())
                .orElseGet(() -> createPendingNode(request));

        return switch (node.getStatus()) {
            case REVOKED -> throw new NodeRevokedException(node.getId());
            case PENDING -> ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(new NodePendingResponse(node.getId(), "pending_approval"));
            case APPROVED -> {
                updateNodeInfo(node, request);
                String token = nodeTokenService.generateToken(node.getId());
                log.info("Node {} re-initialized, token issued", node.getId());
                yield ResponseEntity.ok(new NodeInitializeResponse(
                        node.getId(),
                        token,
                        new NodeInitializeResponse.InitialCommand(heartbeatIntervalSeconds)
                ));
            }
        };
    }

    @Override
    @Transactional
    public HeartbeatResponse heartbeat(String authorizationHeader, HeartbeatRequest request) {
        String tokenNodeId = nodeTokenService.extractNodeId(authorizationHeader);
        if (!tokenNodeId.equals(request.nodeId())) {
            throw new InvalidNodeTokenException("Token subject does not match node_id");
        }

        Node node = nodeRepository.findById(tokenNodeId)
                .orElseThrow(() -> new InvalidNodeTokenException("Unknown node: " + tokenNodeId));
        if (node.getStatus() != NodeStatus.APPROVED) {
            throw new InvalidNodeTokenException("Node is not approved: " + tokenNodeId);
        }

        node.setLastHeartbeatAt(Instant.now());
        saveTelemetry(node.getId(), request);

        // Forward heartbeat to external service
        try {
            String url = "http://147.228.93.216/api/v1/nodes/heartbeat";
            String urlWithParams = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("node_id", tokenNodeId)
                    .toUriString();
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authorizationHeader);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(urlWithParams, HttpMethod.GET, entity, String.class);
            log.info("Forwarded heartbeat GET request for node {}: {}", tokenNodeId, response.getBody());
        } catch (Exception e) {
            log.error("Failed to forward heartbeat GET request for node {}", tokenNodeId, e);
        }

        return powerCommandRepository
                .findFirstByNodeIdAndStatusOrderByCreatedAtAsc(node.getId(), PowerCommand.Status.PENDING)
                .map(cmd -> {
                    cmd.setStatus(PowerCommand.Status.DELIVERED);
                    cmd.setDeliveredAt(Instant.now());
                    log.info("Delivering adjust_power({}) to node {}", cmd.getSetpointPowerW(), node.getId());
                    return HeartbeatResponse.adjustPower(cmd.getSetpointPowerW());
                })
                .orElseGet(HeartbeatResponse::ok);
    }

    @Override
    @Transactional
    public NodeResponse approve(String nodeId) {
        Node node = findNode(nodeId);
        if (node.getStatus() == NodeStatus.REVOKED) {
            throw new NodeRevokedException(nodeId);
        }
        if (node.getStatus() != NodeStatus.APPROVED) {
            node.setStatus(NodeStatus.APPROVED);
            node.setApprovedAt(Instant.now());
            log.info("Node {} approved", nodeId);
        }
        return toNodeResponse(node);
    }

    @Override
    @Transactional
    public NodeResponse revoke(String nodeId) {
        Node node = findNode(nodeId);
        node.setStatus(NodeStatus.REVOKED);
        log.info("Node {} revoked", nodeId);
        return toNodeResponse(node);
    }

    @Override
    @Transactional
    public void requestPowerAdjust(String nodeId, PowerAdjustRequest request) {
        Node node = findNode(nodeId);
        powerCommandRepository.save(PowerCommand.builder()
                .nodeId(node.getId())
                .setpointPowerW(request.setpointPowerW())
                .status(PowerCommand.Status.PENDING)
                .createdAt(Instant.now())
                .build());
        log.info("Queued adjust_power({}) for node {}", request.setpointPowerW(), nodeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NodeResponse> getNodes() {
        return nodeRepository.findAll().stream()
                .map(this::toNodeResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public NodeResponse getNode(String nodeId) {
        return toNodeResponse(findNode(nodeId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TelemetryResponse> getTelemetry(String nodeId, int limit) {
        findNode(nodeId);
        return telemetryRecordRepository
                .findByNodeIdOrderByReceivedAtDesc(nodeId, PageRequest.of(0, limit))
                .stream()
                .map(this::toTelemetryResponse)
                .toList();
    }

    private Node createPendingNode(NodeInitializeRequest request) {
        Node node = Node.builder()
                .id("cn-" + System.currentTimeMillis())
                .hardwareId(request.hardwareId())
                .status(NodeStatus.PENDING)
                .createdAt(Instant.now())
                .build();
        updateNodeInfo(node, request);
        Node saved = nodeRepository.save(node);
        log.info("New node registered as PENDING: {} (hardware {})", saved.getId(), saved.getHardwareId());
        return saved;
    }

    private void updateNodeInfo(Node node, NodeInitializeRequest request) {
        node.setModel(request.model());
        node.setFwVersion(request.fwVersion());
        Optional.ofNullable(request.capabilities()).ifPresent(cap -> {
            node.setMaxPowerW(cap.maxPowerW());
            node.setMinPowerW(cap.minPowerW());
        });
        if (request.gpuInventory() != null) {
            node.replaceGpus(request.gpuInventory().stream()
                    .map(item -> NodeGpu.builder()
                            .gpuId(item.gpuId())
                            .name(item.name())
                            .tdpW(item.tdpW())
                            .computeValue(item.computeCapability() != null ? item.computeCapability().value() : null)
                            .computeUnit(item.computeCapability() != null ? item.computeCapability().unit() : null)
                            .build())
                    .toList());
        }
        nodeRepository.save(node);
    }

    private void saveTelemetry(String nodeId, HeartbeatRequest request) {
        TelemetryRecord record = TelemetryRecord.builder()
                .nodeId(nodeId)
                .receivedAt(Instant.now())
                .reportedAt(request.timestamp())
                .status(request.metrics() != null ? request.metrics().status() : null)
                .systemTempC(request.metrics() != null ? request.metrics().systemTempC() : null)
                .build();

        if (request.gpuTelemetry() != null) {
            request.gpuTelemetry().forEach(item -> record.addGpuSample(GpuTelemetryRecord.builder()
                    .gpuId(item.gpuId())
                    .loadPct(item.loadPct())
                    .tempC(item.tempC())
                    .powerDrawW(item.powerDrawW())
                    .build()));
        }
        telemetryRecordRepository.save(record);
    }

    private Node findNode(String nodeId) {
        return nodeRepository.findById(nodeId)
                .orElseThrow(() -> new NodeNotFoundException(nodeId));
    }

    private NodeResponse toNodeResponse(Node node) {
        boolean online = node.getLastHeartbeatAt() != null
                && node.getLastHeartbeatAt().isAfter(Instant.now()
                .minus(Duration.ofSeconds(heartbeatIntervalSeconds * 2L + 30)));
        return new NodeResponse(
                node.getId(),
                node.getHardwareId(),
                node.getModel(),
                node.getFwVersion(),
                node.getStatus(),
                online,
                node.getMaxPowerW(),
                node.getMinPowerW(),
                node.getCreatedAt(),
                node.getApprovedAt(),
                node.getLastHeartbeatAt(),
                node.getGpus().stream()
                        .map(gpu -> new NodeResponse.GpuResponse(
                                gpu.getGpuId(), gpu.getName(), gpu.getTdpW(),
                                gpu.getComputeValue(), gpu.getComputeUnit()))
                        .toList()
        );
    }

    private TelemetryResponse toTelemetryResponse(TelemetryRecord record) {
        return new TelemetryResponse(
                record.getId(),
                record.getNodeId(),
                record.getReceivedAt(),
                record.getStatus(),
                record.getSystemTempC(),
                record.getGpuSamples().stream()
                        .map(s -> new TelemetryResponse.GpuTelemetryResponse(
                                s.getGpuId(), s.getLoadPct(), s.getTempC(), s.getPowerDrawW()))
                        .toList()
        );
    }
}
