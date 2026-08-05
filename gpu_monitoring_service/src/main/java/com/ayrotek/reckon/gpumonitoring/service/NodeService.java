package com.ayrotek.reckon.gpumonitoring.service;

import com.ayrotek.reckon.gpumonitoring.dto.request.HeartbeatRequest;
import com.ayrotek.reckon.gpumonitoring.dto.request.NodeInitializeRequest;
import com.ayrotek.reckon.gpumonitoring.dto.request.PowerAdjustRequest;
import com.ayrotek.reckon.gpumonitoring.dto.response.HeartbeatResponse;
import com.ayrotek.reckon.gpumonitoring.dto.response.NodeResponse;
import com.ayrotek.reckon.gpumonitoring.dto.response.TelemetryResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface NodeService {

    ResponseEntity<?> initialize(NodeInitializeRequest request);

    HeartbeatResponse heartbeat(String authorizationHeader, HeartbeatRequest request);

    NodeResponse approve(String nodeId);

    NodeResponse revoke(String nodeId);

    void requestPowerAdjust(String nodeId, PowerAdjustRequest request);

    List<NodeResponse> getNodes();

    NodeResponse getNode(String nodeId);

    List<TelemetryResponse> getTelemetry(String nodeId, int limit);
}
