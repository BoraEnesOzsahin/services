package com.ayrotek.reckon.gpumonitoring.repository;

import com.ayrotek.reckon.gpumonitoring.entity.Node;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NodeRepository extends JpaRepository<Node, String> {

    Optional<Node> findByHardwareId(String hardwareId);
}
