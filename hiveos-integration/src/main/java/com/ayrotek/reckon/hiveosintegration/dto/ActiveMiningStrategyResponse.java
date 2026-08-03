package com.ayrotek.reckon.hiveosintegration.dto;

import com.ayrotek.reckon.hiveosintegration.entity.ActiveMiningStrategy;

import java.time.Instant;

public record ActiveMiningStrategyResponse(
        String strategyId,
        Long farmId,
        String strategyName,
        Long flightSheetId,
        String flightSheetName,
        Instant startedAt
) {

    public static ActiveMiningStrategyResponse from(ActiveMiningStrategy active) {
        return new ActiveMiningStrategyResponse(
                active.getStrategyId(),
                active.getFarmId(),
                active.getStrategyName(),
                active.getFlightSheetId(),
                active.getFlightSheetName(),
                active.getStartedAt()
        );
    }
}
