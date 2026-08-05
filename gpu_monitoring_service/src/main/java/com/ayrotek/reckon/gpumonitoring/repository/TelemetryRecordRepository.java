package com.ayrotek.reckon.gpumonitoring.repository;

import com.ayrotek.reckon.gpumonitoring.entity.TelemetryRecord;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TelemetryRecordRepository extends JpaRepository<TelemetryRecord, Long> {

    List<TelemetryRecord> findByNodeIdOrderByReceivedAtDesc(String nodeId, Pageable pageable);
}
