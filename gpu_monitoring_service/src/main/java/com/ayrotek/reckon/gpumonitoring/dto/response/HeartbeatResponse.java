package com.ayrotek.reckon.gpumonitoring.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Heartbeat acknowledgment. When a power command is pending for the node,
 * command="adjust_power" and setpoint_power_w carry the new total power target;
 * the client distributes it across GPUs and applies it via rocm-smi.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record HeartbeatResponse(
        String status,
        String command,
        Integer setpointPowerW
) {

    public static HeartbeatResponse ok() {
        return new HeartbeatResponse("ok", null, null);
    }

    public static HeartbeatResponse adjustPower(int setpointPowerW) {
        return new HeartbeatResponse("ok", "adjust_power", setpointPowerW);
    }
}
