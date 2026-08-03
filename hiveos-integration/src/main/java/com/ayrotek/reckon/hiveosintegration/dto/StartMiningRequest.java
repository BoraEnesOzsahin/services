package com.ayrotek.reckon.hiveosintegration.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;

/**
 * Remote mining trigger.
 *
 * @param coin          PoW coin symbol to mine, e.g. "ETC", "KAS", "RVN"
 * @param algo          HiveOS algorithm id, e.g. "etchash", "kawpow"; resolved from coin when omitted
 * @param pool          pool name as known by HiveOS, e.g. "f2pool", "2miners", "nanopool"
 * @param walletAddress payout wallet address; reused if already registered in the farm
 * @param miner         HiveOS miner id, e.g. "teamredminer", "t-rex", "lolminer", "gminer"
 * @param minerConfig   raw miner_config passed to the flight sheet; when omitted a minimal
 *                      template-based config is built from the pool template
 * @param poolSsl       connect to the pool over SSL
 * @param poolUrls      explicit pool URLs; overrides the ones resolved from HiveOS pool templates
 * @param workerIds     target workers; when omitted the flight sheet is applied to ALL farm workers
 */
public record StartMiningRequest(
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
