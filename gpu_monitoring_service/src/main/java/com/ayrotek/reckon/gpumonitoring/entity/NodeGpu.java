package com.ayrotek.reckon.gpumonitoring.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "node_gpus")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NodeGpu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "node_id")
    private Node node;

    @Column(name = "gpu_id", nullable = false)
    private String gpuId;

    private String name;

    @Column(name = "tdp_w")
    private Integer tdpW;

    @Column(name = "compute_value")
    private Double computeValue;

    @Column(name = "compute_unit")
    private String computeUnit;
}
