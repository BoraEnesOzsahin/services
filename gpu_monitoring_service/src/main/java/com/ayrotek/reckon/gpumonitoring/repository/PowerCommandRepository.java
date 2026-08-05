package com.ayrotek.reckon.gpumonitoring.repository;

import com.ayrotek.reckon.gpumonitoring.entity.PowerCommand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PowerCommandRepository extends JpaRepository<PowerCommand, Long> {

    Optional<PowerCommand> findFirstByNodeIdAndStatusOrderByCreatedAtAsc(String nodeId, PowerCommand.Status status);
}
