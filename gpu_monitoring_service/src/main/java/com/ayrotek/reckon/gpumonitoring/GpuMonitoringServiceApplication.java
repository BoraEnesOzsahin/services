package com.ayrotek.reckon.gpumonitoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class GpuMonitoringServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GpuMonitoringServiceApplication.class, args);
    }

}
