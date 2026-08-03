package com.ayrotek.reckon.hiveosintegration.dto;

import com.ayrotek.reckon.hiveosintegration.entity.MiningStrategy;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record MiningStrategyResponse(
        String id,
        String name,
        Long farmId,
        String coin,
        String algo,
        String pool,
        String walletAddress,
        String miner,
        Map<String, Object> minerConfig,
        Boolean poolSsl,
        List<String> poolUrls,
        List<Long> workerIds,
        Instant createdAt,
        Instant updatedAt
) {

    public static MiningStrategyResponse from(MiningStrategy s) {
        return new MiningStrategyResponse(
                s.getId(),
                s.getName(),
                s.getFarmId(),
                s.getCoin(),
                s.getAlgo(),
                s.getPool(),
                s.getWalletAddress(),
                s.getMiner(),
                s.getMinerConfig(),
                s.getPoolSsl(),
                s.getPoolUrls(),
                s.getWorkerIds(),
                s.getCreatedAt(),
                s.getUpdatedAt()
        );
    }
}
