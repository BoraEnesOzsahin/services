package com.ayrotek.reckon.hiveosintegration.service;

import com.ayrotek.reckon.hiveosintegration.dto.ActiveMiningStrategyResponse;
import com.ayrotek.reckon.hiveosintegration.dto.MiningStrategyRequest;
import com.ayrotek.reckon.hiveosintegration.dto.MiningStrategyResponse;
import com.ayrotek.reckon.hiveosintegration.dto.StartMiningResponse;

import java.util.List;

/**
 * CRUD and execution of reusable, user-owned mining strategies. {@code ownerUserId} comes
 * from the gateway-forwarded X-User-Id header; every operation is scoped to that owner.
 */
public interface MiningStrategyService {

    List<MiningStrategyResponse> list(String ownerUserId);

    MiningStrategyResponse get(String ownerUserId, String id);

    MiningStrategyResponse create(String ownerUserId, MiningStrategyRequest request);

    MiningStrategyResponse update(String ownerUserId, String id, MiningStrategyRequest request);

    void delete(String ownerUserId, String id);

    /** Applies the saved strategy to its farm, starting mining. */
    StartMiningResponse run(String ownerUserId, String id);

    List<ActiveMiningStrategyResponse> listActive(String ownerUserId);

    void clearActive(String ownerUserId, Long farmId);
}
