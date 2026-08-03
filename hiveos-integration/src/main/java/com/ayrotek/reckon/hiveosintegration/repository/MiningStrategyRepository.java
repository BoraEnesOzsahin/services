package com.ayrotek.reckon.hiveosintegration.repository;

import com.ayrotek.reckon.hiveosintegration.entity.MiningStrategy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MiningStrategyRepository extends JpaRepository<MiningStrategy, String> {

    List<MiningStrategy> findByOwnerUserId(String ownerUserId);

    Optional<MiningStrategy> findByIdAndOwnerUserId(String id, String ownerUserId);
}
