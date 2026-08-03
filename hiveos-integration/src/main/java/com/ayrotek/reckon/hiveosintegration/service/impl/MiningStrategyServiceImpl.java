package com.ayrotek.reckon.hiveosintegration.service.impl;

import com.ayrotek.reckon.hiveosintegration.dto.ActiveMiningStrategyResponse;
import com.ayrotek.reckon.hiveosintegration.dto.MiningStrategyRequest;
import com.ayrotek.reckon.hiveosintegration.dto.MiningStrategyResponse;
import com.ayrotek.reckon.hiveosintegration.dto.StartMiningRequest;
import com.ayrotek.reckon.hiveosintegration.dto.StartMiningResponse;
import com.ayrotek.reckon.hiveosintegration.entity.ActiveMiningStrategy;
import com.ayrotek.reckon.hiveosintegration.entity.MiningStrategy;
import com.ayrotek.reckon.hiveosintegration.exception.StrategyNotFoundException;
import com.ayrotek.reckon.hiveosintegration.repository.ActiveMiningStrategyRepository;
import com.ayrotek.reckon.hiveosintegration.repository.MiningStrategyRepository;
import com.ayrotek.reckon.hiveosintegration.service.MiningService;
import com.ayrotek.reckon.hiveosintegration.service.MiningStrategyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MiningStrategyServiceImpl implements MiningStrategyService {

    private final MiningStrategyRepository repository;
    private final ActiveMiningStrategyRepository activeRepository;
    private final MiningService miningService;

    @Override
    @Transactional(readOnly = true)
    public List<MiningStrategyResponse> list(String ownerUserId) {
        return repository.findByOwnerUserId(ownerUserId).stream()
                .map(MiningStrategyResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MiningStrategyResponse get(String ownerUserId, String id) {
        return MiningStrategyResponse.from(require(ownerUserId, id));
    }

    @Override
    @Transactional
    public MiningStrategyResponse create(String ownerUserId, MiningStrategyRequest request) {
        MiningStrategy saved = repository.save(toEntity(new MiningStrategy(), ownerUserId, request));
        log.info("Created mining strategy {} ({}) for owner {}", saved.getId(), saved.getName(), ownerUserId);
        return MiningStrategyResponse.from(saved);
    }

    @Override
    @Transactional
    public MiningStrategyResponse update(String ownerUserId, String id, MiningStrategyRequest request) {
        MiningStrategy saved = repository.save(toEntity(require(ownerUserId, id), ownerUserId, request));
        log.info("Updated mining strategy {} for owner {}", id, ownerUserId);
        return MiningStrategyResponse.from(saved);
    }

    @Override
    @Transactional
    public void delete(String ownerUserId, String id) {
        repository.delete(require(ownerUserId, id));
        activeRepository.deleteByOwnerUserIdAndStrategyId(ownerUserId, id);
        log.info("Deleted mining strategy {} for owner {}", id, ownerUserId);
    }

    @Override
    public StartMiningResponse run(String ownerUserId, String id) {
        MiningStrategy s = require(ownerUserId, id);
        StartMiningRequest request = new StartMiningRequest(
                s.getCoin(),
                s.getAlgo(),
                s.getPool(),
                s.getWalletAddress(),
                s.getMiner(),
                s.getMinerConfig(),
                s.getPoolSsl(),
                s.getPoolUrls(),
                s.getWorkerIds()
        );
        log.info("Running mining strategy {} ({}) on farm {} for owner {}",
                s.getId(), s.getName(), s.getFarmId(), ownerUserId);
        StartMiningResponse response = miningService.startMining(s.getFarmId(), request);
        saveActive(ownerUserId, s, response);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActiveMiningStrategyResponse> listActive(String ownerUserId) {
        return activeRepository.findByOwnerUserId(ownerUserId).stream()
                .map(ActiveMiningStrategyResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public void clearActive(String ownerUserId, Long farmId) {
        activeRepository.deleteByOwnerUserIdAndFarmId(ownerUserId, farmId);
        log.info("Cleared active mining strategy for owner {} farm {}", ownerUserId, farmId);
    }

    private MiningStrategy require(String ownerUserId, String id) {
        return repository.findByIdAndOwnerUserId(id, ownerUserId)
                .orElseThrow(() -> new StrategyNotFoundException(id));
    }

    private MiningStrategy toEntity(MiningStrategy entity, String ownerUserId, MiningStrategyRequest request) {
        entity.setOwnerUserId(ownerUserId);
        entity.setName(request.name());
        entity.setFarmId(request.farmId());
        entity.setCoin(request.coin());
        entity.setAlgo(request.algo());
        entity.setPool(request.pool());
        entity.setWalletAddress(request.walletAddress());
        entity.setMiner(request.miner());
        entity.setMinerConfig(request.minerConfig());
        entity.setPoolSsl(request.poolSsl());
        entity.setPoolUrls(request.poolUrls());
        entity.setWorkerIds(request.workerIds());
        return entity;
    }

    private void saveActive(String ownerUserId, MiningStrategy strategy, StartMiningResponse response) {
        ActiveMiningStrategy active = activeRepository
                .findByOwnerUserIdAndFarmId(ownerUserId, strategy.getFarmId())
                .orElseGet(ActiveMiningStrategy::new);

        active.setOwnerUserId(ownerUserId);
        active.setFarmId(strategy.getFarmId());
        active.setStrategyId(strategy.getId());
        active.setStrategyName(strategy.getName());
        active.setFlightSheetId(response.flightSheetId());
        active.setFlightSheetName(response.flightSheetName());
        active.setStartedAt(Instant.now());
        activeRepository.save(active);
    }
}
