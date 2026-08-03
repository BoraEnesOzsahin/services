package com.ayrotek.reckon.hiveosintegration.repository;

import com.ayrotek.reckon.hiveosintegration.entity.ActiveMiningStrategy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ActiveMiningStrategyRepository extends JpaRepository<ActiveMiningStrategy, String> {

    List<ActiveMiningStrategy> findByOwnerUserId(String ownerUserId);

    Optional<ActiveMiningStrategy> findByOwnerUserIdAndFarmId(String ownerUserId, Long farmId);

    void deleteByOwnerUserIdAndFarmId(String ownerUserId, Long farmId);

    void deleteByOwnerUserIdAndStrategyId(String ownerUserId, String strategyId);
}
