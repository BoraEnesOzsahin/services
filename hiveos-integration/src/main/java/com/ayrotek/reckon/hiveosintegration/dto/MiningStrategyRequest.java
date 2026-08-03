package com.ayrotek.reckon.hiveosintegration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Create/update payload for a reusable mining strategy. Mirrors {@link StartMiningRequest}
 * plus a human-friendly {@code name} and the target {@code farmId}, so a saved strategy
 * carries everything needed to start mining later.
 *
 * @param name          display name of the strategy, unique per user by convention
 * @param farmId        farm the strategy targets
 * @param coin          PoW coin symbol, e.g. "ETC", "KAS", "RVN"
 * @param algo          HiveOS algorithm id, e.g. "etchash"; resolved from coin when omitted
 * @param pool          pool name as known by HiveOS, e.g. "f2pool", "2miners"
 * @param walletAddress payout wallet address
 * @param miner         HiveOS miner id, e.g. "t-rex", "lolminer"
 * @param minerConfig   optional raw miner_config for the flight sheet
 * @param poolSsl       connect to the pool over SSL
 * @param poolUrls      explicit pool URLs; overrides HiveOS pool-template resolution
 * @param workerIds     target workers; when omitted the strategy applies to ALL farm workers
 */
public record MiningStrategyRequest(
        @NotBlank String name,
        @NotNull Long farmId,
        @NotBlank String coin,
        String algo,
        @NotBlank String pool,
        @NotBlank String walletAddress,
        @NotBlank String miner,
        Map<String, Object> minerConfig,
        Boolean poolSsl,
        List<String> poolUrls,
        List<Long> workerIds
) {
}
